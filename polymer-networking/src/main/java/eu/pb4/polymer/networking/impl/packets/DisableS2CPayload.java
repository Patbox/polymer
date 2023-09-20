package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.payload.ContextPayload;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record DisableS2CPayload() implements VersionedPayload {
    public static final Identifier ID = new Identifier("polymer", "disable");

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {

    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static DisableS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new DisableS2CPayload();
    }
}
