package eu.pb4.polymer.mixin.other;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public interface ChunkDeltaUpdateS2CPacketAccessor {
    @Accessor("sectionPos")
    ChunkSectionPos getSectionPos();

    @Accessor("positions")
    short[] getPositions();

    @Accessor("blockStates")
    BlockState[] getBlockStates();
}
