package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolymerCommonPacketListenerExtension {
    void polymer$schedulePacket(Packet<?> packet, int duration);

    void polymer$delayAction(String identifier, int delay, Runnable action);
    static PolymerCommonPacketListenerExtension of(ServerCommonPacketListenerImpl handler) {
        return (PolymerCommonPacketListenerExtension) handler;
    }
}
