package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record PolymerBlockUpdateS2CPayload(BlockPos pos, int blockId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerBlockUpdateS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.WORLD_SET_BLOCK_UPDATE);
    public static final StreamCodec<ContextByteBuf, PolymerBlockUpdateS2CPayload> CODEC = StreamCodec.ofMember(PolymerBlockUpdateS2CPayload::write, PolymerBlockUpdateS2CPayload::read);

    public void write(ContextByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(blockId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static PolymerBlockUpdateS2CPayload read(ContextByteBuf buf) {
        return new PolymerBlockUpdateS2CPayload(buf.readBlockPos(), buf.readVarInt());
    }
}
