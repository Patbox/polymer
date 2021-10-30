package eu.pb4.polymer.impl.interfaces;

import net.minecraft.network.Packet;
import net.minecraft.world.chunk.WorldChunk;

public interface ChunkDataS2CPacketInterface {
    WorldChunk polymer_getWorldChunk();
    Packet<?>[] polymer_getPolymerSyncPackets();
    void polymer_setPolymerSyncPackets(Packet<?>[] packets);
}
