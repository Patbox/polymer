package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CustomPayloadS2CPacket.class)
public class ip_CustomPayloadS2CPacket implements IPAttachedPacket {
    @Unique
    private Packet<?> polymer_ip_attachedPacket = null;

    @Unique
    private RegistryKey<World> polymer_ip_attachedWorld = null;

    @Unique
    private boolean polymer_ip_skip = false;

    @Override
    public Packet<?> polymer_ip_getAttachedPacket() {
        return this.polymer_ip_attachedPacket;
    }

    @Override
    public RegistryKey<World> polymer_ip_getAttachedDimension() {
        return this.polymer_ip_attachedWorld;
    }

    @Override
    public void polymer_ip_setAttachedPacket(Packet<?> packet, RegistryKey<World> world) {
        this.polymer_ip_attachedPacket = packet;
        this.polymer_ip_attachedWorld = world;
    }

    @Override
    public boolean polymer_ip_shouldSkip() {
        return this.polymer_ip_skip;
    }

    @Override
    public CustomPayloadS2CPacket polymer_ip_setSkip(boolean value) {
        this.polymer_ip_skip = value;
        return ((CustomPayloadS2CPacket) (Object) this);
    }
}
