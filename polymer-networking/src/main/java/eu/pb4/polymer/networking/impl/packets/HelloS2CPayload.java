package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record HelloS2CPayload() implements VersionedPayload {
    public static final Identifier ID = new Identifier("polymer", "hello");

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {

    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static HelloS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new HelloS2CPayload();
    }
}
