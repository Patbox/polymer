package eu.pb4.polymer.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public interface VirtualHeadBlock extends VirtualBlock {
    String getVirtualHeadSkin(BlockState state);


    default CompoundTag getVirtualHeadSkullOwner(BlockState state) {
        CompoundTag skullOwner = new CompoundTag();
        CompoundTag properties = new CompoundTag();
        CompoundTag data = new CompoundTag();
        ListTag textures = new ListTag();
        textures.add(data);

        data.putString("Value", ((VirtualHeadBlock) state.getBlock()).getVirtualHeadSkin(state));
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        skullOwner.putIntArray("Id", new int[] { 0, 0, 0, 0 });

        return skullOwner;
    }

    default Packet<?> getVirtualHeadPacket(BlockState state, BlockPos pos) {
        CompoundTag main = new CompoundTag();
        CompoundTag skullOwner = this.getVirtualHeadSkullOwner(state);
        main.putString("id", "minecraft:skull");
        main.put("SkullOwner", skullOwner);
        main.putInt("x", pos.getX());
        main.putInt("y", pos.getY());
        main.putInt("z", pos.getZ());
        return new BlockEntityUpdateS2CPacket(pos, 4, main);
    }

}
