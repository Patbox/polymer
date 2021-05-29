package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.entity.VirtualEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MobSpawnS2CPacket.class, priority = 500)
public class MobSpawnS2CPacketMixin {
    @Shadow @Mutable
    private int entityTypeId;

    @Inject(method = "<init>(Lnet/minecraft/entity/LivingEntity;)V", at = @At(value = "TAIL"))
    private void replaceWithVirtual(LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof VirtualEntity) {
            this.entityTypeId = Registry.ENTITY_TYPE.getRawId(((VirtualEntity) entity).getVirtualEntityType());
        }
    }
}
