package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
public class PolymerServerProtocolHandler {
    public static void handle(ServerPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        int version = -1;

        try {
            version = buf.readVarInt();
            handle(handler, identifier.getPath(), version, buf);
        } catch (Exception e) {
            PolymerMod.LOGGER.error(String.format("Invalid %s (%s) packet received from client %s (%s)!", identifier, version, handler.getPlayer().getName().getString(), handler.getPlayer().getUuidAsString()));
            PolymerMod.LOGGER.error(e);
        }

    }


    private static void handle(ServerPlayNetworkHandler handler, String packet, int version, PacketByteBuf buf) {
        switch (packet) {
            case ClientPackets.HANDSHAKE -> handleHandshake(handler, version, buf);
            case ClientPackets.SYNC_REQUEST -> handleSyncRequest(handler, version, buf);
            case ClientPackets.WORLD_PICK_BLOCK -> handlePickBlock(handler, version, buf);
            case ClientPackets.WORLD_PICK_ENTITY -> handlePickEntity(handler, version, buf);
            case ClientPackets.CHANGE_TOOLTIP -> handleTooltipChange(handler, version, buf);
        }
    }

    private static void handleTooltipChange(ServerPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        if (version == 0) {
            polymerHandler.polymer_setAdvancedTooltip(buf.readBoolean());
            if (polymerHandler.polymer_lastSyncUpdate() != 0) {
                PolymerServerProtocol.syncVanillaItemGroups(handler);
                PolymerSyncUtils.synchronizeCreativeTabs(handler);
                PolymerUtils.reloadInventory(handler.player);
            }
        }
    }

    private static void handleSyncRequest(ServerPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);
        var ver = polymerHandler.polymer_lastSyncUpdate();

        if (polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_STARTED) == 0 && System.currentTimeMillis() - polymerHandler.polymer_lastSyncUpdate() > 1000 * 20) {
            polymerHandler.polymer_saveSyncTime();
            PolymerServerProtocol.sendSyncPackets(handler);

            if (ver == 0 && handler.getPlayer() != null) {
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

    private static void handleHandshake(ServerPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        if (version == 0 && !polymerHandler.polymer_hasPolymer()) {
            polymerHandler.polymer_setVersion(buf.readString(64));

            var size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                var id = buf.readString();

                var size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                polymerHandler.polymer_setSupportedVersion(id, ServerPackets.getBestSupported(id, list.elements()));
            }

            PolymerServerProtocol.sendHandshake(handler);
        }
    }

    private static void handlePickBlock(ServerPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
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

    private static void handlePickEntity(ServerPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var isCreative = handler.getPlayer().isCreative();

            var id = buf.readVarInt();

            var entity = handler.player.world.getEntityById(id);

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
