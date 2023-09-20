package eu.pb4.polymer.networking.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface NetworkHandlerExtension {

    long polymerNet$lastPacketUpdate(Identifier identifier);
    void polymerNet$savePacketTime(Identifier identifier);
    static NetworkHandlerExtension of(ServerPlayerEntity player) {
        return (NetworkHandlerExtension) player.networkHandler;
    }

    ClientConnection polymerNet$getConnection();

    static NetworkHandlerExtension of(ServerCommonNetworkHandler handler) {
        return (NetworkHandlerExtension) handler;
    }
}