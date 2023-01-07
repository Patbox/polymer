package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.core.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.impl.other.PolymerTooltipContext;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PolymerImplUtils {
    public static final SimpleEvent<BiConsumer<Registry<Object>, Object>> ON_REGISTERED = new SimpleEvent<>();
    public static final Collection<BlockState> POLYMER_STATES = ((PolymerIdList<BlockState>) Block.STATE_IDS).polymer$getPolymerEntries();

    public static Identifier toItemGroupId(ItemGroup group) {
        var posId = InternalServerRegistry.ITEM_GROUPS.getId(group);
        if (posId != null) {
            return posId;
        }

        if (CompatStatus.FABRIC_ITEM_GROUP) {
            try {
                return ((net.fabricmc.fabric.api.itemgroup.v1.IdentifiableItemGroup) group).getId();
            } catch (Throwable e) {

            }
        }

        var name = group.getDisplayName();

        if (name.getContent() instanceof TranslatableTextContent content) {
            var id = content.getKey();

            if (id.startsWith("itemGroup.")) {
                id = id.substring("itemGroup.".length());
            }

            var sub = id.split("\\.", 2);

            if (sub.length == 2) {
                return new Identifier(makeSafeId(sub[0]), makeSafeId(sub[1]));
            } else {
                return new Identifier(makeSafeId(id));
            }
        }

        return new Identifier(makeSafeId(name.getString()));
    }

    private static String makeSafeId(String id) {
        if (Identifier.isValid(id)) {
            return id;
        }

        id = id.toLowerCase(Locale.ROOT);
        var builder = new StringBuilder();

        for (int i = 0; i < id.length(); i++) {
            var chr = id.charAt(i);

            if (Identifier.isPathCharacterValid(chr)) {
                builder.append(chr);
            } else {
                builder.append("_");
            }
        }

        return builder.toString();
    }

    public static Identifier id(String path) {
        return new Identifier(PolymerUtils.ID, path);
    }

    public static ItemStack readStack(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = buf.readVarInt();
            int j = buf.readByte();
            var dec = decodeItem(i);
            if (dec == null || dec == Items.AIR) {
                buf.readNbt();
                return ItemStack.EMPTY;
            }

            ItemStack itemStack = new ItemStack(dec, j);
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

        builder.append(Registries.BLOCK.getId(state.getBlock()));

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
                for (var reg : ((Registry<Registry<Object>>) Registries.REGISTRIES)) {
                    msg.accept("");
                    msg.accept("== Registry: " + ((Registry<Object>) (Object) Registries.REGISTRIES).getId(reg).toString());
                    msg.accept("");
                    if (reg instanceof RegistrySyncExtension regEx) {
                        msg.accept("= Status: " + regEx.polymer_registry_sync$getStatus().name());
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
        return ((PolymerIdList) Block.STATE_IDS).polymer$getOffset();
    }

    public static void setStateIdsLock(boolean value) {
        ((PolymerIdList) Block.STATE_IDS).polymer$setReorderLock(value);
    }

    public static boolean getStateIdsLock(boolean value) {
        return ((PolymerIdList) Block.STATE_IDS).polymer$getReorderLock();
    }

    public static void invokeRegistered(Registry<Object> ts, Object entry) {
        ON_REGISTERED.invoke((a) -> a.accept(ts, entry));
    }

    public static boolean shouldSkipStateInitialization(Stream<StackWalker.StackFrame> s) {
        if (CompatStatus.QUILT_REGISTRY) {
            var x = s.skip(3).findFirst();
            return x.isPresent() && x.get().getMethodName().contains("lambda$onInit");
        }
        return false;
    }

    public static boolean shouldLogStateRebuild(StackTraceElement[] trace) {
        return trace.length <= 4 || !trace[4].getClassName().startsWith("org.quiltmc.qsl.registry.impl.sync");
    }

    public static boolean isPolymerControlled(ItemStack stack) {
        return PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getServerIdentifier(stack) != null || PolymerUtils.isServerOnly(stack);
    }

    public static PolymerTooltipContext getTooltipContext(ServerPlayerEntity player) {
        return player != null && player.networkHandler instanceof PolymerNetworkHandlerExtension h && h.polymer$advancedTooltip() ? PolymerTooltipContext.ADVANCED : PolymerTooltipContext.BASIC;
    }

    public static boolean areSamePolymerType(ItemStack a, ItemStack b) {
        return Objects.equals(PolymerItemUtils.getServerIdentifier(a), PolymerItemUtils.getServerIdentifier(b));
    }

    public static boolean isServerSideSyncableEntry(Registry reg, Object obj) {
        return PolymerUtils.isServerOnly(obj) || (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC && !reg.getId(obj).getNamespace().equals("minecraft"));
    }

    public static ItemStack convertStack(ItemStack representation, ServerPlayerEntity player) {
        return convertStack(representation, player, PolymerUtils.getTooltipContext(player));
    }

    public static ItemStack convertStack(ItemStack representation, ServerPlayerEntity player, TooltipContext context) {
        return ServerTranslationUtils.parseFor(player.networkHandler,  PolyMcUtils.toVanilla(PolymerItemUtils.getPolymerItemStack(representation, context, player), player));
    }
}
