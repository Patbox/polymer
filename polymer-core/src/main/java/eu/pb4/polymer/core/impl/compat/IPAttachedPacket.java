package eu.pb4.polymer.core.impl.compat;

import net.minecraft.network.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IPAttachedPacket {
    void polymer_ip_setAttachedPacket(Packet<?> packet, RegistryKey<World> worldRegistryKey);
    Packet<?> polymer_ip_getAttachedPacket();
    RegistryKey<World> polymer_ip_getAttachedWorld();
}
