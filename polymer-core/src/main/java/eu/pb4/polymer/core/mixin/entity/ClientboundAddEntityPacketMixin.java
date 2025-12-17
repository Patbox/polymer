package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;


@Mixin(ClientboundAddEntityPacket.class)
public class ClientboundAddEntityPacketMixin {
    @Shadow @Final private EntityType<?> type;

    @Shadow @Final private int id;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object polymer$replaceWithPolymer(@Nullable Object value) {
        var entity = EntityAttachedPacket.get(this, this.id);
        var polymerEntity = PolymerEntity.get(entity);
        if (polymerEntity != null && value == entity.getType()) {
            return polymerEntity.getPolymerEntityType(PacketContext.get());
        } else {
            return value;
        }
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;writeVarInt(I)Lnet/minecraft/network/FriendlyByteBuf;", ordinal = 1))
    private int polymer$replaceValue(int data) {
        if (this.type == EntityType.FALLING_BLOCK) {
            return Block.getId(PolymerBlockUtils.getPolymerBlockState(Block.stateById(data), PacketContext.get()));
        }

        return data;
    }
}
