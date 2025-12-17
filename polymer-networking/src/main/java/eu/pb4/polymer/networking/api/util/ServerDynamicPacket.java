package eu.pb4.polymer.networking.api.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@FunctionalInterface
public interface ServerDynamicPacket extends Packet<ClientCommonPacketListener> {
    static Packet<ClientCommonPacketListener> of(BiFunction<ServerCommonPacketListenerImpl, @Nullable ServerPlayer, Packet<ClientCommonPacketListener>> builder) {
        return (ServerDynamicPacket) builder::apply;
    }

    Packet<ClientCommonPacketListener> createPacket(ServerCommonPacketListenerImpl handler, @Nullable ServerPlayer player);


    @Override
    default void handle(ClientCommonPacketListener listener) {
        throw new UnsupportedOperationException("This is not real packet!");
    }

    @Override
    default PacketType<ServerDynamicPacket> type() {
        throw new UnsupportedOperationException("This is not real packet!");
    }


    @Override
    default boolean isSkippable() {
        return true;
    }
}
