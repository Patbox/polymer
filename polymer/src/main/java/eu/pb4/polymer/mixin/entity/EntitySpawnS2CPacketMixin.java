package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.impl.interfaces.PlayerAwarePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(EntitySpawnS2CPacket.class)
public class EntitySpawnS2CPacketMixin implements PlayerAwarePacket {

    @Shadow @Final private EntityType<?> entityTypeId;

    @Shadow @Final private int entityData;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
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

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeInt(I)Lio/netty/buffer/ByteBuf;", ordinal = 0))
    private int polymer_replaceValue(int data) {
        if (this.entityTypeId == EntityType.FALLING_BLOCK) {
            return Block.getRawIdFromState(PolymerBlockUtils.getPolymerBlockState(Block.getStateFromRawId(data), PolymerUtils.getPlayer()));
        }

        return data;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getEntityData", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceClientData(CallbackInfoReturnable<Integer> cir) {
        if (this.entityTypeId == EntityType.FALLING_BLOCK && this.entityData >= PolymerClientUtils.getBlockStateOffset()) {
            var state = InternalClientRegistry.getRealBlockState(this.entityData - PolymerClientUtils.getBlockStateOffset() + 1);
            cir.setReturnValue(Block.getRawIdFromState(state));
        }
    }
}
