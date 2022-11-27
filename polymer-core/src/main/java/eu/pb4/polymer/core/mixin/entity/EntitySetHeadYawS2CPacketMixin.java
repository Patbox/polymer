package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySetHeadYawS2CPacket.class)
public class EntitySetHeadYawS2CPacketMixin {
    @Shadow @Mutable
    private byte headYaw;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;B)V", at = @At("TAIL"))
    private void polymer$replaceHeadYaw(Entity entity, byte headYaw, CallbackInfo ci) {
        if (entity instanceof PolymerEntity virtualEntity) {
            this.headYaw = (byte)((int)(virtualEntity.getClientSideHeadYaw(entity.getHeadYaw()) * 256.0F / 360.0F));
        }
    }
}
