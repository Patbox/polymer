package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerBlockUpdateS2CPayload(BlockPos pos, int blockId) implements VersionedPayload {
    public static final Identifier ID = S2CPackets.WORLD_SET_BLOCK_UPDATE;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(blockId);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerBlockUpdateS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerBlockUpdateS2CPayload(buf.readBlockPos(), buf.readVarInt());
    }
}
