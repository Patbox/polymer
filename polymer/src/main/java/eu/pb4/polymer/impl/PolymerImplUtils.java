package eu.pb4.polymer.impl;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PolymerImplUtils {
    public static final Text[] ICON;
    private static final ThreadLocal<ServerPlayerEntity> playerTargetHack = new ThreadLocal<>();
    private static ItemStack NO_TEXTURE;

    public static final SimpleEvent<BiConsumer<Registry<Object>, Object>> ON_REGISTERED = new SimpleEvent<>();

    static {
        final String chr = "█";
        var icon = new ArrayList<MutableText>();
        try {
            var source = ImageIO.read(PolymerImpl.getJarPath("assets/icon_ingame.png").toUri().toURL());

            for (int y = 0; y < source.getHeight(); y++) {
                var base = Text.empty();
                int line = 0;
                int color = source.getRGB(0, y) & 0xFFFFFF;
                for (int x = 0; x < source.getWidth(); x++) {
                    int colorPixel = source.getRGB(x, y) & 0xFFFFFF;

                    if (color == colorPixel) {
                        line++;
                    } else {
                        base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color)));
                        color = colorPixel;
                        line = 1;
                    }
                }

                base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color)));
                icon.add(base);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            icon.add(Text.literal("/!\\ [ Invalid icon file ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true)));
        }

        ICON = icon.toArray(new Text[0]);
    }

    @Nullable
    public static ServerPlayerEntity getPlayer() {
        return playerTargetHack.get();
    }

    public static void setPlayer(ServerPlayerEntity player) {
        playerTargetHack.set(player);
    }

    public static Predicate<ServerCommandSource> permission(String path, int operatorLevel) {
        if (CompatStatus.FABRIC_PERMISSION_API_V0) {
            return Permissions.require("polymer." + path, operatorLevel);
        } else {
            return source -> source.hasPermissionLevel(operatorLevel);
        }
    }

    public static Identifier id(String path) {
        return new Identifier(PolymerUtils.ID, path);
    }

    public static ItemStack getNoTextureItem() {
        if (NO_TEXTURE == null) {
            NO_TEXTURE = Items.PLAYER_HEAD.getDefaultStack();
            NO_TEXTURE.getOrCreateNbt().put("SkullOwner", PolymerUtils.createSkullOwner(PolymerUtils.NO_TEXTURE_HEAD_VALUE));
            NO_TEXTURE.setCustomName(Text.empty());
        }
        return NO_TEXTURE;
    }

    public static ItemStack readStack(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = buf.readVarInt();
            int j = buf.readByte();
            ItemStack itemStack = new ItemStack(decodeItem(i), j);
            itemStack.setNbt(buf.readNbt());
            return itemStack;
        }
    }

    public static Item decodeItem(int rawId) {
        if (PolymerImpl.IS_CLIENT) {
            return InternalClientRegistry.decodeItem(rawId);
        } else {
            return Item.byRawId(rawId);
        }
    }

    /**
     * Why you may ask? Some mods just like to make my life harder by modifying vanilla packet format...
     * So method above would get invalid data
     */
    public static void writeStack(PacketByteBuf buf, ItemStack stack) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            Item item = stack.getItem();
            buf.writeVarInt(Item.getRawId(item));
            buf.writeByte(stack.getCount());
            buf.writeNbt(stack.getNbt());
        }
    }

    public static String getAsString(BlockState state) {
        var builder = new StringBuilder();

        builder.append(Registry.BLOCK.getId(state.getBlock()));

        if (!state.getEntries().isEmpty()) {
            builder.append("[");
            var iterator = state.getEntries().entrySet().iterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                builder.append(entry.getKey().getName());
                builder.append("=");
                builder.append(((Property) entry.getKey()).name(entry.getValue()));

                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }

        return builder.toString();
    }

    @Nullable
    public static String dumpRegistry() {
        BufferedWriter writer = null;
        try {
            var path = "./polymer-dump-" + FabricLoader.getInstance().getEnvironmentType().name().toLowerCase(Locale.ROOT) + ".txt";
            writer = new BufferedWriter(new FileWriter(path));
            BufferedWriter finalWriter = writer;
            Consumer<String> msg = (str) -> {
                try {
                    finalWriter.write(str);
                    finalWriter.newLine();
                } catch (Exception e) {
                    // Silence;
                }
            };


            {
                msg.accept("== Vanilla Registries");
                for (var reg : ((Registry<Registry<Object>>) Registry.REGISTRIES)) {
                    msg.accept("");
                    msg.accept("== Registry: " + ((Registry<Object>) (Object) Registry.REGISTRIES).getId(reg).toString());
                    msg.accept("");
                    if (reg instanceof RegistrySyncExtension regEx) {
                        msg.accept("= Status: " + regEx.polymerRSM_getStatus().name());
                        msg.accept("");
                    }

                    if (CompatStatus.FABRIC_SYNC) {
                        msg.accept("= Synced: " + RegistryAttributeHolder.get(reg).hasAttribute(RegistryAttribute.SYNCED));
                        msg.accept("");
                    }

                    for (var entry : reg) {
                        msg.accept("" + reg.getRawId(entry) + " | " + reg.getId(entry).toString() + " | Polymer? " + PolymerUtils.isServerOnly(entry));
                    }
                }
                msg.accept("");
                msg.accept("== BlockStates");
                msg.accept("");
                msg.accept("= Polymer Starts: " + PolymerImplUtils.getBlockStateOffset());
                msg.accept("");
                msg.accept("= Vanilla ChunkDeltaUpdateS2CPacket broken: " + (PolymerImplUtils.getBlockStateOffset() >= 524288));
                msg.accept("");

                for (var state : Block.STATE_IDS) {
                    msg.accept(Block.STATE_IDS.getRawId(state) + " | " + getAsString(state) + " | Polymer? " + (state.getBlock() instanceof PolymerBlock));
                }
            }

            {
                msg.accept("");
                msg.accept("== Polymer Registries");
                msg.accept("");
                var reg = InternalServerRegistry.ITEM_GROUPS;

                msg.accept("== Registry: ItemGroup");
                msg.accept("");

                for (var entry : reg) {
                    msg.accept(reg.getRawId(entry) + " | " + reg.getId(entry));
                }

                if (PolymerImpl.IS_CLIENT) {
                    for (var reg2 : ((Collection<ImplPolymerRegistry<Object>>) (Object) InternalClientRegistry.REGISTRIES)) {
                        msg.accept("");
                        msg.accept("== Registry: " + reg2.getName() + " (Client)");
                        msg.accept("");
                        for (var entry : reg2) {
                            msg.accept(reg2.getRawId(entry) + " | " + reg2.getId(entry));
                        }
                        msg.accept("");
                        msg.accept("=== Tags:");
                        msg.accept("");
                        for (var tag : reg2.getTags()) {
                            msg.accept(tag + " | :");
                            for (var entry : reg2.getTag(tag)) {
                                msg.accept("  " + reg2.getId(entry));
                            }
                        }
                    }

                    msg.accept("");
                    msg.accept("== Registry: BlockState (Client)");
                    msg.accept("");

                    for (var entry : InternalClientRegistry.BLOCK_STATES) {
                        msg.accept(InternalClientRegistry.BLOCK_STATES.getRawId(entry) + " | " + entry.block().identifier());
                    }
                }
            }

            try {
                writer.close();
            } catch (Exception e) {
            }

            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static int getBlockStateOffset() {
        return ((PolymerIdList) Block.STATE_IDS).polymer_getOffset();
    }

    public static void setStateIdsLock(boolean value) {
        ((PolymerIdList) Block.STATE_IDS).polymer_setReorderLock(value);
    }

    public static boolean getStateIdsLock(boolean value) {
        return ((PolymerIdList) Block.STATE_IDS).polymer_getReorderLock();
    }

    public static void invokeRegistered(Registry<Object> ts, Object entry) {
        ON_REGISTERED.invoke((a) -> a.accept(ts, entry));
    }

    public static <T> T createUnsafe(Class<T> tClass) {
        try {
            return (T) UnsafeAccess.UNSAFE.allocateInstance(tClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean shouldSkipStateInitialization(Stream<StackWalker.StackFrame> s) {
        if (CompatStatus.QUILT_REGISTRY) {
            var x = s.skip(3).findFirst();
            return x.isPresent() && x.get().getMethodName().contains("lambda$onInit");
        }
        return false;
    }

    public static boolean shouldLogStateRebuild(StackTraceElement[] trace) {
        if (trace.length > 4 && trace[4].getClassName().startsWith("org.quiltmc.qsl.registry.impl.sync")) {
            return false;
        }


        return true;
    }
}
