package eu.pb4.polymer.virtualentity.mixin;

import net.minecraft.network.packet.s2c.play.SetCameraEntityS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SetCameraEntityS2CPacket.class)
public interface SetCameraEntityS2CPacketAccessor {
    @Mutable
    @Accessor
    void setEntityId(int id);
}
