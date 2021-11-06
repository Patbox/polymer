package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ServerPacketHandler {
    public static void handle(ServerPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        switch (identifier.getPath()) {
            case PolymerPacketIds.VERSION -> {
                var ver = polymerHandler.polymer_protocolVersion();
                polymerHandler.polymer_setVersion(Math.max(buf.readShort(), -1), buf.readString(64));

                if (System.currentTimeMillis() - polymerHandler.polymer_lastSyncUpdate() > 1000 * 20) {
                    polymerHandler.polymer_saveSyncTime();
                    PolymerServerProtocol.sendSyncPackets(handler);

                    if (ver == -2 && handler.getPlayer() != null) {
                        var world = handler.getPlayer().getServerWorld();
                        int dist = ((ThreadedAnvilChunkStorageAccessor) handler.getPlayer().getServerWorld().getChunkManager().threadedAnvilChunkStorage).getWatchDistance();
                        int playerX = handler.player.getWatchedSection().getX();
                        int playerZ = handler.player.getWatchedSection().getZ();

                        for (int x = -dist; x <= dist; x++) {
                            for (int z = -dist; z <= dist; z++) {
                                var chunk = (WorldChunk) world.getChunk(x + playerX, z + playerZ, ChunkStatus.FULL, false);

                                if (chunk != null) {
                                    PolymerServerProtocol.sendSectionUpdate(handler, chunk);
                                }
                            }
                        }
                    }
                }
            }
            case PolymerPacketIds.PICK_BLOCK -> {
                var isCreative = handler.getPlayer().isCreative();

                var pos = buf.readBlockPos();
                var ctr = buf.readBoolean();

                if (pos.getManhattanDistance(handler.player.getBlockPos()) <= 32) {
                    BlockState blockState = handler.player.world.getBlockState(pos);
                    if (blockState.isAir()) {
                        return;
                    }

                    Block block = blockState.getBlock();
                    var itemStack = block.getPickStack(handler.player.world, pos, blockState);
                    if (itemStack.isEmpty()) {
                        return;
                    }

                    BlockEntity blockEntity = null;
                    if (isCreative && ctr && blockState.hasBlockEntity()) {
                        blockEntity = handler.player.world.getBlockEntity(pos);
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
            }
        }
    }

    private static void addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
        NbtCompound nbtCompound = blockEntity.writeNbt(new NbtCompound());
        NbtCompound nbtCompound3;
        if (stack.getItem() instanceof SkullItem && nbtCompound.contains("SkullOwner")) {
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
