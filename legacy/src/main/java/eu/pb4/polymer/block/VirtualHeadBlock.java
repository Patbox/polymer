package eu.pb4.polymer.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Use {@link eu.pb4.polymer.api.block.PolymerHeadBlock} instead
 */
@Deprecated
public interface VirtualHeadBlock extends VirtualBlock {
    String getVirtualHeadSkin(BlockState state);

    default Block getVirtualBlock() {
        return Blocks.PLAYER_HEAD;
    }

    default NbtCompound getVirtualHeadSkullOwner(BlockState state) {
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound data = new NbtCompound();
        NbtList textures = new NbtList();
        textures.add(data);

        data.putString("Value", ((VirtualHeadBlock) state.getBlock()).getVirtualHeadSkin(state));
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        skullOwner.putIntArray("Id", new int[] { 0, 0, 0, 0 });

        return skullOwner;
    }

    default Packet<?> getVirtualHeadPacket(BlockState state, BlockPos pos) {
        NbtCompound main = new NbtCompound();
        NbtCompound skullOwner = this.getVirtualHeadSkullOwner(state);
        main.putString("id", "minecraft:skull");
        main.put("SkullOwner", skullOwner);
        main.putInt("x", pos.getX());
        main.putInt("y", pos.getY());
        main.putInt("z", pos.getZ());
        return new BlockEntityUpdateS2CPacket(pos, BlockEntityUpdateS2CPacket.SKULL, main);
    }

    default void sendPacketsAfterCreation(ServerPlayerEntity player, BlockPos pos, BlockState blockState) {
        player.networkHandler.sendPacket(this.getVirtualHeadPacket(blockState, pos));
    }
}
