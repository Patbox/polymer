package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EntitySpawnS2CPacket.class)
public class EntitySpawnS2CPacketMixin {

    @Shadow @Final private EntityType<?> entityTypeId;

    @Shadow @Final private int entityData;

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

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;)V"))
    private Object polymer_replaceWithPolymer(@Nullable Object value) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity polymerEntity && value == ((Entity) polymerEntity).getType()) {
            return polymerEntity.getPolymerEntityType(PolymerUtils.getPlayer());
        } else {
            return value;
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getEntityTypeId", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceWithPolymer(CallbackInfoReturnable<EntityType<?>> cir) {
        if (EntityAttachedPacket.get(this) instanceof PolymerEntity polymerEntity && !PolymerClientDecoded.checkDecode(polymerEntity)) {
            cir.setReturnValue(polymerEntity.getPolymerEntityType(ClientUtils.getPlayer()));
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;"))
    private Object polymer_replaceWithPolymer(PacketByteBuf instance, IndexedIterable<?> registry) {
        return InternalClientRegistry.decodeEntity(instance.readVarInt());
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;", ordinal = 0))
    private int polymer_replaceValue(int data) {
        if (this.entityTypeId == EntityType.FALLING_BLOCK) {
            return Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId(data), PolymerUtils.getPlayer()));
        }

        return data;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;I)V", at = @At("TAIL"))
    private void polymer_changePosition(Entity entity, int entityData, CallbackInfo ci) {
        if (entity instanceof PolymerEntity virtualEntity) {
            Vec3d vec3d = virtualEntity.getClientSidePosition(entity.getPos());
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.yaw = (byte)((int)(virtualEntity.getClientSideYaw(entity.getYaw()) * 256.0F / 360.0F));
            this.pitch = (byte)((int)(virtualEntity.getClientSidePitch(entity.getPitch()) * 256.0F / 360.0F));
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/LivingEntity;I)V", at = @At("TAIL"))
    private void polymer_changePosition2(LivingEntity entity, int entityTypeId, CallbackInfo ci) {
        if (entity instanceof PolymerEntity virtualEntity) {
            Vec3d vec3d = virtualEntity.getClientSidePosition(entity.getPos());
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.yaw = (byte)((int)(virtualEntity.getClientSideYaw(entity.getYaw()) * 256.0F / 360.0F));
            this.pitch = (byte)((int)(virtualEntity.getClientSidePitch(entity.getPitch()) * 256.0F / 360.0F));
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getEntityData", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceClientData(CallbackInfoReturnable<Integer> cir) {
        if (this.entityTypeId == EntityType.FALLING_BLOCK) {
            var state = InternalClientRegistry.decodeState(this.entityData);
            cir.setReturnValue(Block.getRawIdFromState(state));
        }
    }
}
