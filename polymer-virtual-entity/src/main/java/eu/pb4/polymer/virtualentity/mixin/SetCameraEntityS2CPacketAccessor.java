package eu.pb4.polymer.virtualentity.mixin;

import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetCameraPacket.class)
public interface SetCameraEntityS2CPacketAccessor {
    @Mutable
    @Accessor
    void setCameraId(int id);
}
