package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.entity.VirtualEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
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

    @Shadow @Mutable
    private double x;

    @Shadow @Mutable
    private double y;

    @Shadow @Mutable
    private double z;

    @Shadow @Mutable
    private byte yaw;

    @Shadow @Mutable
    private byte pitch;

    @Shadow @Mutable
    private byte headYaw;

    @Inject(method = "<init>(Lnet/minecraft/entity/LivingEntity;)V", at = @At(value = "TAIL"))
    private void replaceWithVirtual(LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof VirtualEntity ve) {
            this.entityTypeId = Registry.ENTITY_TYPE.getRawId(ve.getVirtualEntityType());

            Vec3d vec3d = ve.getClientSidePosition(entity.getPos());
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.yaw = (byte)((int)(ve.getClientSideYaw(entity.getYaw()) * 256.0F / 360.0F));
            this.pitch = (byte)((int)(ve.getClientSidePitch(entity.getPitch()) * 256.0F / 360.0F));
            this.headYaw = (byte)((int)(ve.getClientSideHeadYaw(entity.headYaw) * 256.0F / 360.0F));
        }
    }
}
