package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
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

    @Shadow @Final private EntityType<?> entityType;

    @Shadow @Final private int id;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object polymer$replaceWithPolymer(@Nullable Object value) {
        if (EntityAttachedPacket.get(this, this.id) instanceof PolymerEntity polymerEntity && value == ((Entity) polymerEntity).getType()) {
            return polymerEntity.getPolymerEntityType(PolymerUtils.getPlayerContext());
        } else {
            return value;
        }
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;", ordinal = 1))
    private int polymer$replaceValue(int data) {
        if (this.entityType == EntityType.FALLING_BLOCK) {
            return Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId(data), PolymerUtils.getPlayerContext()));
        }

        return data;
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/network/EntityTrackerEntry;I)V", at = @At("TAIL"))
    private void polymer$changePosition(Entity entity, EntityTrackerEntry entityTrackerEntry, int entityData, CallbackInfo ci) {
        if (entity instanceof PolymerEntity virtualEntity) {
            Vec3d vec3d = virtualEntity.getClientSidePosition(entityTrackerEntry.getPos());
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.yaw = (byte)((int)(virtualEntity.getClientSideYaw(entityTrackerEntry.getYaw()) * 256.0F / 360.0F));
            this.pitch = (byte)((int)(virtualEntity.getClientSidePitch(entityTrackerEntry.getPitch()) * 256.0F / 360.0F));
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;ILnet/minecraft/util/math/BlockPos;)V", at = @At("TAIL"))
    private void polymer$changePosition2(Entity entity, int entityTypeId, BlockPos pos, CallbackInfo ci) {
        if (entity instanceof PolymerEntity virtualEntity) {
            Vec3d vec3d = virtualEntity.getClientSidePosition(entity.getPos());
            this.x = vec3d.x;
            this.y = vec3d.y;
            this.z = vec3d.z;
            this.yaw = (byte)((int)(virtualEntity.getClientSideYaw(entity.getYaw()) * 256.0F / 360.0F));
            this.pitch = (byte)((int)(virtualEntity.getClientSidePitch(entity.getPitch()) * 256.0F / 360.0F));
        }
    }
}
