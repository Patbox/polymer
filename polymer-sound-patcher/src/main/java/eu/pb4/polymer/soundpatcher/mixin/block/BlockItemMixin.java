package eu.pb4.polymer.soundpatcher.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.soundpatcher.impl.CoreBridge;
import eu.pb4.polymer.soundpatcher.impl.SoundRemapperImpl;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;


@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void wrapPlaySound(World instance, Entity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, Operation<Void> original, @Local(ordinal = 1) BlockState state) {
        original.call(instance,
                SoundRemapperImpl.ignoreExceptions(CoreBridge.getClientSideSoundGroup(state, PacketContext.create(instance.getRegistryManager())).getPlaceSound()) ? null : source,
                pos, sound, category, volume, pitch);
    }

}
