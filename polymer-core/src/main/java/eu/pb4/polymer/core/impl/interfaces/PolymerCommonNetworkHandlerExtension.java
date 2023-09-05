package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.block.BlockMapper;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolymerCommonNetworkHandlerExtension {
    void polymer$schedulePacket(Packet<?> packet, int duration);

    void polymer$delayAction(String identifier, int delay, Runnable action);
    static PolymerCommonNetworkHandlerExtension of(ServerCommonNetworkHandler handler) {
        return (PolymerCommonNetworkHandlerExtension) handler;
    }
}
