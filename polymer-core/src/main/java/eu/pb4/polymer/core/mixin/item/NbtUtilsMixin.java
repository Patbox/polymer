package eu.pb4.polymer.core.mixin.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtUtils.class)
public class NbtUtilsMixin {
    @Inject(method = "writeBlockState", at = @At("RETURN"))
    private static void polymerCore$markNbt(BlockState state, CallbackInfoReturnable<CompoundTag> cir) {
        //((TypeAwareNbtCompound) cir.getReturnValue()).polymerCore$setType(TypeAwareNbtCompound.STATE_TYPE);
    }
}
