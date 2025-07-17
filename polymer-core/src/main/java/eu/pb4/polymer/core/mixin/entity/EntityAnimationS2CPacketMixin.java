package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityAnimationS2CPacket.class)
public class EntityAnimationS2CPacketMixin {
    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;I)V", at = @At("TAIL"))
    private void attachEntity(Entity entity, int animationId, CallbackInfo ci) {
        EntityAttachedPacket.set(this, entity);
    }
}
