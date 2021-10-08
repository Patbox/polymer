package eu.pb4.polymer.other;

import net.minecraft.network.Packet;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface PolymerNetworkHandlerExtension {
    boolean polymer_hasResourcePack();

    void polymer_setResourcePack(boolean value);

    void polymer_schedulePacket(Packet<?> packet, int duration);
}
