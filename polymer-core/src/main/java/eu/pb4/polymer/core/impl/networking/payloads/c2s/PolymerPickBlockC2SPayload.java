package eu.pb4.polymer.core.impl.networking.payloads.c2s;

import eu.pb4.polymer.core.impl.networking.C2SPackets;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerPickBlockC2SPayload(BlockPos pos, boolean control) implements VersionedPayload {
    public static final Identifier ID = C2SPackets.WORLD_PICK_BLOCK;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(control);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerPickBlockC2SPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerPickBlockC2SPayload(buf.readBlockPos(), buf.readBoolean());
    }
}
