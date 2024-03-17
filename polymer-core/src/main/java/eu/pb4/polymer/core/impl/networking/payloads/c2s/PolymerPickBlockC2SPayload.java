package eu.pb4.polymer.core.impl.networking.payloads.c2s;

import eu.pb4.polymer.core.impl.networking.C2SPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerPickBlockC2SPayload(BlockPos pos, boolean control) implements CustomPayload {
    public static final CustomPayload.Id<DisableS2CPayload> ID = new CustomPayload.Id<>(C2SPackets.WORLD_PICK_BLOCK);
    public static final PacketCodec<ContextByteBuf, PolymerPickBlockC2SPayload> CODEC = PacketCodec.of(PolymerPickBlockC2SPayload::write, PolymerPickBlockC2SPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(control);
    }


    public static PolymerPickBlockC2SPayload read(PacketByteBuf buf) {
        return new PolymerPickBlockC2SPayload(buf.readBlockPos(), buf.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
