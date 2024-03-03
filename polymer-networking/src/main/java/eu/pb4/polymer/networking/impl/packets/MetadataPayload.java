package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public record MetadataPayload(Map<Identifier, NbtElement> map) implements VersionedPayload {
    public static final Identifier ID = new Identifier("polymer", "metadata");

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        // todo
       // buf.writeMap(map, PacketByteBuf::writeIdentifier, PacketByteBuf::writeNbt);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static MetadataPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new MetadataPayload(buf.readMap(PacketByteBuf::readIdentifier, (bufx) -> bufx.readNbt(NbtSizeTracker.ofUnlimitedBytes())));
    }
}
