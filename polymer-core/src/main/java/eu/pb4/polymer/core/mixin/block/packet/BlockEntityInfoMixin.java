package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.impl.CommonImplPacketKeys;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundLevelChunkPacketData$BlockEntityInfo")
public class BlockEntityInfoMixin {
    @ModifyExpressionValue(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getUpdateTag(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;"))
    private static CompoundTag changeNbt(CompoundTag original, @Local(argsOnly = true) BlockEntity blockEntity) {
        var x = PacketContext.get();
        if (x == null) {
            PolymerImplUtils.warnMissingContextChunkPacket();
            return original;
        }

        var registryAccess = x.get(PacketContext.REGISTRY_ACCESS);
        if (registryAccess == null) {
            PolymerImplUtils.warnMissingContextChunkPacket();
            return original;
        }
        return PolymerBlockUtils.transformBlockEntityNbt(x, blockEntity.getType(), original, registryAccess);
    }
}
