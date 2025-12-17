package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import eu.pb4.polymer.core.impl.interfaces.PolymerGamePacketListenerExtension;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class PolymerImplUtils {
    public static final ThreadLocal<Unit> IS_RELOADING_WORLD = new ThreadLocal<>();
    public static final ThreadLocal<Unit> IGNORE_PLAY_SOUND_EXCLUSION = new ThreadLocal<>();
    public static final Collection<BlockState> POLYMER_STATES = ((PolymerIdMapper<BlockState>) Block.BLOCK_STATE_REGISTRY).polymer$getPolymerEntries();
    public static final HolderLookup.Provider FALLBACK_LOOKUP = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(PolymerUtils.ID, path);
    }

    public static String getAsString(BlockState state) {
        var builder = new StringBuilder();

        builder.append(BuiltInRegistries.BLOCK.getKey(state.getBlock()));

        if (!state.getValues().isEmpty()) {
            builder.append("[");
            var iterator = state.getValues().entrySet().iterator();

            while (iterator.hasNext()) {
                var entry = iterator.next();
                builder.append(entry.getKey().getName());
                builder.append("=");
                builder.append(((Property) entry.getKey()).getName(entry.getValue()));

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
                for (var reg : ((Registry<Registry<Object>>) BuiltInRegistries.REGISTRY)) {
                    msg.accept("");
                    msg.accept("== Registry: " + ((Registry<Object>) (Object) BuiltInRegistries.REGISTRY).getKey(reg).toString());
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
                        msg.accept("" + reg.getId(entry) + " | " + reg.getKey(entry).toString() + " | Polymer? " + PolymerUtils.isServerOnly(reg, entry));
                    }
                }
                msg.accept("");
                msg.accept("== BlockStates");
                msg.accept("");
                msg.accept("= Polymer Starts: " + PolymerImplUtils.getBlockStateOffset());
                msg.accept("");
                msg.accept("= All States: " + Block.BLOCK_STATE_REGISTRY.size());

                //noinspection unchecked
                var pl = (PolymerIdMapper<BlockState>) Block.BLOCK_STATE_REGISTRY;
                msg.accept("= Polymer States: " + pl.polymer$getPolymerEntries().size());
                msg.accept("= Server Bits: " + Mth.ceillog2(Block.BLOCK_STATE_REGISTRY.size()));
                msg.accept("= Vanilla Bits: " + pl.polymer$getVanillaBitCount());
                msg.accept("= NonPolymer Bits: " + pl.polymer$getNonPolymerBitCount());
                msg.accept("");

                for (var state : Block.BLOCK_STATE_REGISTRY) {
                    msg.accept(Block.BLOCK_STATE_REGISTRY.getId(state) + " | " + getAsString(state) + " | Polymer? " + (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock())));
                }
            }

            msg.accept("");
            msg.accept("== Server/Local Polymer Item Groups");
            msg.accept("");
            for (var entry : InternalServerRegistry.ITEM_GROUPS) {
                msg.accept(InternalServerRegistry.ITEM_GROUPS.getEntryId(entry).toString());
            }

            {
                msg.accept("");
                msg.accept("== Polymer Registries");
                msg.accept("");

                if (PolymerImpl.IS_CLIENT) {
                    for (var reg2 : ((Collection<ImplPolymerRegistry<Object>>) (Object) InternalClientRegistry.REGISTRIES)) {
                        msg.accept("");
                        msg.accept("== Registry: " + reg2.getName() + " (Client)");
                        msg.accept("");
                        for (var entry : reg2) {
                            msg.accept(reg2.getId(entry) + " | " + reg2.getId(entry));
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
                        msg.accept(InternalClientRegistry.BLOCK_STATES.getId(entry) + " | " + entry.block().identifier());
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
        return ((PolymerIdMapper) Block.BLOCK_STATE_REGISTRY).polymer$getOffset();
    }

    public static boolean removeFromItemGroup(ItemStack stack) {
        if (stack == null) {
            return true;
        }
        return isPolymerControlled(stack);
    }
    public static boolean isPolymerControlled(ItemStack stack) {
        return PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getServerIdentifier(stack) != null;
    }

    public static PolymerTooltipType getTooltipContext(ServerPlayer player) {
        return player != null && player.connection instanceof PolymerGamePacketListenerExtension h && h.polymer$advancedTooltip() ? PolymerTooltipType.ADVANCED : PolymerTooltipType.BASIC;
    }

    public static boolean isServerSideSyncableEntry(@SuppressWarnings("rawtypes") Registry reg, Object obj) {
        return PolymerUtils.isServerOnly(reg, obj) || (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC && PolyMcUtils.isServerSide(reg, obj));
    }

    public static ItemStack convertStack(ItemStack representation, ServerPlayer player) {
        return convertStack(representation, player, PolymerUtils.getTooltipType(player));
    }

    public static ItemStack convertStack(ItemStack representation, ServerPlayer player, TooltipFlag context) {
        return ServerTranslationUtils.parseFor(player.connection, PolyMcUtils.toVanilla(PolymerItemUtils.getPolymerItemStack(representation, context, PacketContext.create(player)), player));
    }

    public static void callItemGroupEvents(Identifier id, CreativeModeTab itemGroup, List<ItemStack> parentTabStacks, List<ItemStack> searchTabStacks, CreativeModeTab.ItemDisplayParameters context) {
        if (CompatStatus.FABRIC_ITEM_GROUP) {
            try {
                var fabricCollector = new FabricItemGroupEntries(context, parentTabStacks, searchTabStacks);
                ItemGroupEvents.modifyEntriesEvent(ResourceKey.create(Registries.CREATIVE_MODE_TAB, id)).invoker().modifyEntries(fabricCollector);
                ItemGroupEvents.MODIFY_ENTRIES_ALL.invoker().modifyEntries(itemGroup, fabricCollector);
            } catch (Throwable e) {
                if (PolymerImpl.LOG_MORE_ERRORS) {
                    PolymerImpl.LOGGER.warn("Failed to execute Fabric Item Group event!", e);
                }
            }
        }
    }

    @Nullable
    public static String getModName(ItemStack stack) {
        var id = PolymerItemUtils.getServerIdentifier(stack);
        if (id != null) {
            return getModName(id);
        }
        return null;
    }

    public static String getModName(Identifier id) {
        var container = FabricLoader.getInstance().getModContainer(id.getNamespace());
        return container.isPresent() ? container.get().getMetadata().getName() : (id.getNamespace() + "*");
    }
}
