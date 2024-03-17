package eu.pb4.polymer.networking.impl.packets;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public record MetadataPayload(Map<Identifier, NbtElement> map) implements CustomPayload {
    public static final Id<MetadataPayload> ID = PolymerNetworking.id("polymer", "metadata");
    public static final PacketCodec<ContextByteBuf, MetadataPayload> CODEC = PacketCodec.of(MetadataPayload::write, MetadataPayload::read);
    public void write(ContextByteBuf buf) {
        buf.writeMap(map, PacketByteBuf::writeIdentifier, (x, n) -> x.writeNbt(n));
    }

    public static MetadataPayload read(ContextByteBuf buf) {
        return new MetadataPayload(buf.readMap(PacketByteBuf::readIdentifier, (bufx) -> bufx.readNbt(NbtSizeTracker.ofUnlimitedBytes())));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
