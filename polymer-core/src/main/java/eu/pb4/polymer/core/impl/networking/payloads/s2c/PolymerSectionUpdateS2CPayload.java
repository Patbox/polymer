package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record PolymerSectionUpdateS2CPayload(SectionPos chunkPos, short[] pos, int[] blocks)  implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerSectionUpdateS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.WORLD_CHUNK_SECTION_UPDATE);
    public static final StreamCodec<ContextByteBuf, PolymerSectionUpdateS2CPayload> CODEC = StreamCodec.ofMember(PolymerSectionUpdateS2CPayload::write, PolymerSectionUpdateS2CPayload::read);

    public void write(FriendlyByteBuf buf) {
        SectionPos.STREAM_CODEC.encode(buf, this.chunkPos);
        buf.writeVarInt(this.pos.length);
        for (int i = 0; i < this.pos.length; i++) {
            buf.writeVarLong((long) this.blocks[i] << 12 | (long)this.pos[i]);
        }
    }

    public static PolymerSectionUpdateS2CPayload read(FriendlyByteBuf buf) {
        var chunkPos = SectionPos.STREAM_CODEC.decode(buf);
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
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
