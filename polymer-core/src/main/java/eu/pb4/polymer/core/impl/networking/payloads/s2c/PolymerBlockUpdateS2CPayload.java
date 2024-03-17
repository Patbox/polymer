package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerBlockUpdateS2CPayload(BlockPos pos, int blockId) implements CustomPayload {
    public static final CustomPayload.Id<PolymerBlockUpdateS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.WORLD_SET_BLOCK_UPDATE);
    public static final PacketCodec<ContextByteBuf, PolymerBlockUpdateS2CPayload> CODEC = PacketCodec.of(PolymerBlockUpdateS2CPayload::write, PolymerBlockUpdateS2CPayload::read);

    public void write(ContextByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(blockId);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static PolymerBlockUpdateS2CPayload read(ContextByteBuf buf) {
        return new PolymerBlockUpdateS2CPayload(buf.readBlockPos(), buf.readVarInt());
    }
}
