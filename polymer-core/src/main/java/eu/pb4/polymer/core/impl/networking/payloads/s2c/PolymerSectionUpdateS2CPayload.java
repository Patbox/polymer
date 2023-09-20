package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerSectionUpdateS2CPayload(ChunkSectionPos chunkPos, short[] pos, int[] blocks) implements VersionedPayload {
    public static final Identifier ID = S2CPackets.WORLD_CHUNK_SECTION_UPDATE;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeChunkSectionPos(this.chunkPos);
        buf.writeVarInt(this.pos.length);
        for (int i = 0; i < this.pos.length; i++) {
            buf.writeVarLong((long) this.blocks[i] << 12 | (long)this.pos[i]);
        }
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerSectionUpdateS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        var chunkPos = ChunkSectionPos.from(buf.readLong());
        int i = buf.readVarInt();
        var pos = new short[i];
        var blocks = new int[i];

        for(int j = 0; j < i; ++j) {
            long l = buf.readVarLong();
            pos[j] = (short)((int)(l & 4095L));
            blocks[j] = (int)(l >>> 12);
        }


        return new PolymerSectionUpdateS2CPayload(chunkPos, pos, blocks);
    }
}
