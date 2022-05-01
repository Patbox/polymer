package eu.pb4.polymer.impl.compat;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IPAttachedPacket {
    Packet<?> polymer_ip_getAttachedPacket();
    RegistryKey<World> polymer_ip_getAttachedDimension();
    void polymer_ip_setAttachedPacket(Packet<?> packet, RegistryKey<World> worldRegistryKey);
    boolean polymer_ip_shouldSkip();
    CustomPayloadS2CPacket polymer_ip_setSkip(boolean value);
}
