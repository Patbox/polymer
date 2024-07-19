package eu.pb4.polymer.core.impl;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PolymerImplUtils {
    public static final ThreadLocal<RegistryWrapper.WrapperLookup> WRAPPER_LOOKUP_PASSER = new ThreadLocal<>();

    public static final Collection<BlockState> POLYMER_STATES = ((PolymerIdList<BlockState>) Block.STATE_IDS).polymer$getPolymerEntries();
    public static final RegistryWrapper.WrapperLookup FALLBACK_LOOKUP = DynamicRegistryManager.of(Registries.REGISTRIES);

    public static Identifier id(String path) {
        return Identifier.of(PolymerUtils.ID, path);
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
                msg.accept("= All States: " + Block.STATE_IDS.size());

                //noinspection unchecked
                var pl = (PolymerIdList<BlockState>) Block.STATE_IDS;
                msg.accept("= Polymer States: " + pl.polymer$getPolymerEntries().size());
                msg.accept("= Server Bits: " + MathHelper.ceilLog2(Block.STATE_IDS.size()));
                msg.accept("= Vanilla Bits: " + pl.polymer$getVanillaBitCount());
                msg.accept("= NonPolymer Bits: " + pl.polymer$getNonPolymerBitCount());
                msg.accept("");

                for (var state : Block.STATE_IDS) {
                    msg.accept(Block.STATE_IDS.getRawId(state) + " | " + getAsString(state) + " | Polymer? " + (state.getBlock() instanceof PolymerBlock));
                }
            }

            msg.accept("");
            msg.accept("== Server/Local Polymer Item Groups");
            msg.accept("");
            for (var entry : InternalServerRegistry.ITEM_GROUPS) {
                msg.accept(InternalServerRegistry.ITEM_GROUPS.getId(entry).toString());
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

    public static boolean removeFromItemGroup(ItemStack stack) {
        if (stack == null) {
            return true;
        }
        return isPolymerControlled(stack);
    }
    public static boolean isPolymerControlled(ItemStack stack) {
        return PolymerItemUtils.isPolymerServerItem(stack) || PolymerItemUtils.getServerIdentifier(stack) != null || PolymerUtils.isServerOnly(stack);
    }

    public static PolymerTooltipType getTooltipContext(ServerPlayerEntity player) {
        return player != null && player.networkHandler instanceof PolymerPlayNetworkHandlerExtension h && h.polymer$advancedTooltip() ? PolymerTooltipType.ADVANCED : PolymerTooltipType.BASIC;
    }

    public static boolean isServerSideSyncableEntry(Registry reg, Object obj) {
        return PolymerUtils.isServerOnly(obj) || (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC && PolyMcUtils.isServerSide(reg, obj));
    }

    public static ItemStack convertStack(ItemStack representation, ServerPlayerEntity player) {
        return convertStack(representation, player, PolymerUtils.getTooltipType(player));
    }

    public static ItemStack convertStack(ItemStack representation, ServerPlayerEntity player, TooltipType context) {
        return ServerTranslationUtils.parseFor(player.networkHandler, PolyMcUtils.toVanilla(PolymerItemUtils.getPolymerItemStack(representation, context, player.getRegistryManager(), player), player));
    }

    public static void pickBlock(ServerPlayerEntity player, BlockPos pos, boolean withNbt) {
        var isCreative = player.isCreative();

        BlockState blockState = player.getWorld().getBlockState(pos);
        if (blockState.isAir()) {
            return;
        }

        Block block = blockState.getBlock();
        var itemStack = block.getPickStack(player.getWorld(), pos, blockState);
        if (itemStack.isEmpty()) {
            return;
        }

        if (isCreative && withNbt && blockState.hasBlockEntity()) {
            var blockEntity = player.getWorld().getBlockEntity(pos);
            if (blockEntity != null && (!blockEntity.copyItemDataRequiresOperator() || player.isCreativeLevelTwoOp())) {
                NbtCompound nbtCompound = blockEntity.createComponentlessNbtWithIdentifyingData(player.getRegistryManager());
                //noinspection deprecation
                blockEntity.removeFromCopiedStackNbt(nbtCompound);
                BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), nbtCompound);
                itemStack.applyComponentsFrom(blockEntity.createComponentMap());
            }
        }


        PlayerInventory playerInventory = player.getInventory();


        int i = playerInventory.getSlotWithStack(itemStack);
        if (isCreative) {
            playerInventory.addPickBlock(itemStack);
            player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
        } else if (i != -1) {
            if (PlayerInventory.isValidHotbarIndex(i)) {
                playerInventory.selectedSlot = i;
            } else {
                player.getInventory().swapSlotWithHotbar(i);
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, playerInventory.selectedSlot, playerInventory.getStack(playerInventory.selectedSlot)));
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, i, playerInventory.getStack(i)));
            }
            player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
        }
    }

    public static void pickEntity(ServerPlayerEntity player, Entity entity) {
        var isCreative = player.isCreative();

        var itemStack = entity.getPickBlockStack();

        if (itemStack != null && !itemStack.isEmpty()) {
            PlayerInventory playerInventory = player.getInventory();
            int i = playerInventory.getSlotWithStack(itemStack);
            if (isCreative) {
                playerInventory.addPickBlock(itemStack);
                player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
            } else if (i != -1) {
                if (PlayerInventory.isValidHotbarIndex(i)) {
                    playerInventory.selectedSlot = i;
                } else {
                    player.getInventory().swapSlotWithHotbar(i);
                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, playerInventory.selectedSlot, playerInventory.getStack(playerInventory.selectedSlot)));
                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, i, playerInventory.getStack(i)));
                }
                player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
            }
        }
    }
}
