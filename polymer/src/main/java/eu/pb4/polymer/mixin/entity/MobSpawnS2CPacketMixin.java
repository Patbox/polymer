package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MobSpawnS2CPacket.class, priority = 500)
public class MobSpawnS2CPacketMixin {

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

    @Mutable
    @Shadow @Final private int entityTypeId;

    @Inject(method = "<init>(Lnet/minecraft/entity/LivingEntity;)V", at = @At(value = "TAIL"))
    private void polymer_replaceWithPolymer(LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof PolymerEntity ve) {
            Vec3d vec3d = ve.getClientSidePosition(entity.getPos());
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.yaw = (byte)((int)(ve.getClientSideYaw(entity.getYaw()) * 256.0F / 360.0F));
            this.pitch = (byte)((int)(ve.getClientSidePitch(entity.getPitch()) * 256.0F / 360.0F));
            this.headYaw = (byte)((int)(ve.getClientSideHeadYaw(entity.headYaw) * 256.0F / 360.0F));
        }
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;", ordinal = 1))
    private int polymer_replaceWithPolymer(int value) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity polymerEntity) {
            return Registry.ENTITY_TYPE.getRawId(polymerEntity.getPolymerEntityType(PolymerUtils.getPlayer()));
        } else {
            return value;
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void polymer_replaceWithPolymerSync(PacketByteBuf buf, CallbackInfo ci) {
        this.entityTypeId = Registry.ENTITY_TYPE.getRawId(InternalClientRegistry.decodeEntity(this.entityTypeId));
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getEntityTypeId", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceWithPolymer(CallbackInfoReturnable<Integer> cir) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity polymerEntity && !PolymerClientDecoded.checkDecode(polymerEntity)) {
            cir.setReturnValue(Registry.ENTITY_TYPE.getRawId(polymerEntity.getPolymerEntityType(PolymerUtils.getPlayer())));
        }
    }

}
