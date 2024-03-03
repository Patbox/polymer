package eu.pb4.polymer.core.mixin.item;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NbtHelper.class)
public class NbtHelperMixin {
    @Inject(method = "fromBlockState", at = @At("RETURN"))
    private static void polymerCore$markNbt(BlockState state, CallbackInfoReturnable<NbtCompound> cir) {
        //((TypeAwareNbtCompound) cir.getReturnValue()).polymerCore$setType(TypeAwareNbtCompound.STATE_TYPE);
    }
}
