package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundAnimatePacket.class)
public class ClientboundAnimatePacketMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;I)V", at = @At("TAIL"))
    private void attachEntity(Entity entity, int animationId, CallbackInfo ci) {
        EntityAttachedPacket.set(this, entity);
    }
}
