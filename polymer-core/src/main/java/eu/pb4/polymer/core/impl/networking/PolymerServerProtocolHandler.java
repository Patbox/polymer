package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerChangeTooltipC2SPayload;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerPickBlockC2SPayload;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerPickEntityC2SPayload;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerServerProtocolHandler {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        PolymerServerNetworking.registerPlayHandler(PolymerPickBlockC2SPayload.class, PolymerServerProtocolHandler::handlePickBlock);
        PolymerServerNetworking.registerPlayHandler(PolymerPickEntityC2SPayload.class, PolymerServerProtocolHandler::handlePickEntity);
        PolymerServerNetworking.registerPlayHandler(PolymerChangeTooltipC2SPayload.class, PolymerServerProtocolHandler::handleTooltipChange);

        PolymerServerNetworking.ON_PLAY_SYNC.register((handler, x) -> {
            PolymerServerProtocol.sendSyncPackets(handler, true);
        });

        ServerMetadataKeys.setup();
        S2CPackets.SYNC_ENCHANTMENT.getNamespace();
        C2SPackets.WORLD_PICK_BLOCK.getNamespace();
    }

    private static void handleTooltipChange(MinecraftServer server, ServerPlayNetworkHandler handler, PolymerChangeTooltipC2SPayload payload) {
        handler.getPlayer().getServer().execute(() -> {
            PolymerPlayNetworkHandlerExtension.of(handler).polymer$setAdvancedTooltip(payload.advanced());

            if (PolymerServerNetworking.getLastPacketReceivedTime(handler, C2SPackets.CHANGE_TOOLTIP) + 1000 < System.currentTimeMillis()) {
                PolymerSyncUtils.synchronizeCreativeTabs(handler);
                PolymerUtils.reloadInventory(handler.player);
            }
        });
    }

    private static void handlePickBlock(MinecraftServer server, ServerPlayNetworkHandler handler, PolymerPickBlockC2SPayload payload) {
        var pos = payload.pos();
        var ctr = payload.control();


        server.execute(() -> {
            var isCreative = handler.getPlayer().isCreative();

            if (pos.getManhattanDistance(handler.player.getBlockPos()) <= 32) {
                BlockState blockState = handler.player.getWorld().getBlockState(pos);
                if (blockState.isAir()) {
                    return;
                }

                Block block = blockState.getBlock();
                var itemStack = block.getPickStack(handler.player.getWorld(), pos, blockState);
                if (itemStack.isEmpty()) {
                    return;
                }

                BlockEntity blockEntity = null;
                if (isCreative && ctr && blockState.hasBlockEntity()) {
                    blockEntity = handler.player.getWorld().getBlockEntity(pos);
                }


                PlayerInventory playerInventory = handler.player.getInventory();
                if (blockEntity != null) {
                    addBlockEntityNbt(itemStack, blockEntity);
                }

                int i = playerInventory.getSlotWithStack(itemStack);
                if (isCreative) {
                    playerInventory.addPickBlock(itemStack);
                    handler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
                } else if (i != -1) {
                    if (PlayerInventory.isValidHotbarIndex(i)) {
                        playerInventory.selectedSlot = i;
                    } else {
                        handler.player.getInventory().swapSlotWithHotbar(i);
                        handler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, playerInventory.selectedSlot, playerInventory.getStack(playerInventory.selectedSlot)));
                        handler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, i, playerInventory.getStack(i)));
                    }
                    handler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
                }
            }
        });
    }

    private static void handlePickEntity(MinecraftServer server, ServerPlayNetworkHandler handler, PolymerPickEntityC2SPayload payload) {
        var isCreative = handler.getPlayer().isCreative();

        server.execute(() -> {

            var entity = handler.player.getServerWorld().getEntityById(payload.entityId());

            if (entity != null && entity.getPos().relativize(handler.player.getPos()).lengthSquared() < 1024) {
                var itemStack = entity.getPickBlockStack();

                if (itemStack != null && !itemStack.isEmpty()) {
                    PlayerInventory playerInventory = handler.player.getInventory();
                    int i = playerInventory.getSlotWithStack(itemStack);
                    if (isCreative) {
                        playerInventory.addPickBlock(itemStack);
                        handler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
                    } else if (i != -1) {
                        if (PlayerInventory.isValidHotbarIndex(i)) {
                            playerInventory.selectedSlot = i;
                        } else {
                            handler.player.getInventory().swapSlotWithHotbar(i);
                            handler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, playerInventory.selectedSlot, playerInventory.getStack(playerInventory.selectedSlot)));
                            handler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, i, playerInventory.getStack(i)));
                        }
                        handler.sendPacket(new UpdateSelectedSlotS2CPacket(playerInventory.selectedSlot));
                    }
                }
            }
        });
    }

    private static void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
        NbtCompound nbtCompound = blockEntity.createNbtWithId();
        NbtCompound nbtCompound3;
        if (stack.getItem() instanceof PlayerHeadItem && nbtCompound.contains("SkullOwner")) {
            nbtCompound3 = nbtCompound.getCompound("SkullOwner");
            stack.getOrCreateNbt().put("SkullOwner", nbtCompound3);
        } else {
            stack.setSubNbt("BlockEntityTag", nbtCompound);
            nbtCompound3 = new NbtCompound();
            NbtList nbtList = new NbtList();
            nbtList.add(NbtString.of("\"(+NBT)\""));
            nbtCompound3.put("Lore", nbtList);
            stack.setSubNbt("display", nbtCompound3);
        }
    }
}
