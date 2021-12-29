package eu.pb4.polymer.mixin.block.packet;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public interface ChunkDeltaUpdateS2CPacketAccessor {
    @Accessor("sectionPos")
    ChunkSectionPos polymer_getSectionPos();

    @Accessor("positions")
    short[] polymer_getPositions();

    @Accessor("blockStates")
    BlockState[] polymer_getBlockStates();
}
