package eu.pb4.polymer.mixin.block;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntityUpdateS2CPacket.class)
public interface BlockEntityUpdateS2CPacketAccessor {
    @Invoker
    static BlockEntityUpdateS2CPacket createBlockEntityUpdateS2CPacket(BlockPos blockPos, BlockEntityType<?> blockEntityType, NbtCompound nbtCompound) {
        throw new UnsupportedOperationException();
    }
}
