package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerSectionUpdateS2CPayload(ChunkSectionPos chunkPos, short[] pos, int[] blocks)  implements CustomPayload {
    public static final CustomPayload.Id<PolymerSectionUpdateS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.WORLD_CHUNK_SECTION_UPDATE);
    public static final PacketCodec<ContextByteBuf, PolymerSectionUpdateS2CPayload> CODEC = PacketCodec.of(PolymerSectionUpdateS2CPayload::write, PolymerSectionUpdateS2CPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeChunkSectionPos(this.chunkPos);
        buf.writeVarInt(this.pos.length);
        for (int i = 0; i < this.pos.length; i++) {
            buf.writeVarLong((long) this.blocks[i] << 12 | (long)this.pos[i]);
        }
    }

    public static PolymerSectionUpdateS2CPayload read(PacketByteBuf buf) {
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

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
