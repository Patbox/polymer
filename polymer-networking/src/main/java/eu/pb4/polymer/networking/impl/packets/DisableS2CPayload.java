package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record DisableS2CPayload() implements CustomPacketPayload {
    public static final Type<DisableS2CPayload> ID = PolymerNetworking.id("polymer", "disable");
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
