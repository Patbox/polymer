package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomPayloadS2CPacket.class)
public class ip_CustomPayloadS2CPacket implements IPAttachedPacket {
    @Unique
    private Packet<?> polymer_ip_attachedPacket = null;

    @Unique
    private RegistryKey<World> polymer_ip_attachedWorld = null;

    @Override
    public void polymer_ip_setAttachedPacket(Packet<?> packet, RegistryKey<World> worldRegistryKey) {
        this.polymer_ip_attachedPacket = packet;
        this.polymer_ip_attachedWorld = worldRegistryKey;
    }

    @Override
    public Packet<?> polymer_ip_getAttachedPacket() {
        return this.polymer_ip_attachedPacket;
    }

    @Override
    public RegistryKey<World> polymer_ip_getAttachedWorld() {
        return this.polymer_ip_attachedWorld;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void polymer_ip_writePacket(PacketByteBuf buf, CallbackInfo ci) {
        if (this.polymer_ip_attachedPacket != null) {
            this.polymer_ip_attachedPacket.write(buf);
        }
    }

    @Inject(method = "getData", at = @At("RETURN"))
    private void polymer_ip_writePacket(CallbackInfoReturnable<PacketByteBuf> cir) {
        if (this.polymer_ip_attachedPacket != null) {
            this.polymer_ip_attachedPacket.write(cir.getReturnValue());
        }
    }
}
