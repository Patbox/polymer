package eu.pb4.polymer.mixin.block;

import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDataS2CPacket.class)
public interface ChunkDataS2CPacketAccessor {
    @Accessor(value = "chunkX")
    int getChunkX();

    @Accessor(value = "chunkZ")
    int getChunkZ();
}
