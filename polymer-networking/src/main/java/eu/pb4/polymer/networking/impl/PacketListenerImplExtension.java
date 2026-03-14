package eu.pb4.polymer.networking.impl;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PacketListenerImplExtension {

    long polymerNet$lastPacketUpdate(Identifier identifier);
    void polymerNet$savePacketTime(Identifier identifier);
    static PacketListenerImplExtension of(ServerPlayer player) {
        return (PacketListenerImplExtension) player.connection;
    }

    Connection polymerNet$getConnection();

    @Nullable
    RegistryAccess polymer$getDynamicRegistryManager();

    static PacketListenerImplExtension of(ServerCommonPacketListenerImpl handler) {
        return (PacketListenerImplExtension) handler;
    }
}