package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.PlayerAwarePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
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

    @Redirect(method = "<init>(Lnet/minecraft/entity/Entity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;"))
    private static EntityType<?> polymer_replaceWithVirtual(Entity entity) {
        if (entity instanceof PolymerEntity polymerEntity) {
            return polymerEntity.getPolymerEntityType();
        } else {
            return entity.getType();
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
        if (this.entityTypeId == EntityType.FALLING_BLOCK && this.entityData >= PolymerBlockUtils.BLOCK_STATE_OFFSET) {
            var state = InternalClientRegistry.getRealBlockState(this.entityData - PolymerBlockUtils.BLOCK_STATE_OFFSET + 1);
            cir.setReturnValue(Block.getRawIdFromState(state));
        }
    }
}
