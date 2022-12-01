package eu.pb4.polymer.networking.api;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.BiFunction;

public interface DynamicPacket extends Packet<ClientPlayPacketListener> {
    static Packet<ClientPlayPacketListener> of(BiFunction<ServerPlayNetworkHandler, ServerPlayerEntity, Packet<ClientPlayPacketListener>> builder) {
        return (DynamicPacket) builder::apply;
    }

    Packet<ClientPlayPacketListener> createPacket(ServerPlayNetworkHandler handler, ServerPlayerEntity player);

    @Override
    default void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void apply(ClientPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isWritingErrorSkippable() {
        return true;
    }
}
