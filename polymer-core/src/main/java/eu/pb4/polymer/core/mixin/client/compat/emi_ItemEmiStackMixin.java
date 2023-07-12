package eu.pb4.polymer.core.mixin.client.compat;

import dev.emi.emi.api.stack.ItemEmiStack;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Pseudo
@Mixin(ItemEmiStack.class)
public abstract class emi_ItemEmiStackMixin {
    @Shadow public abstract ItemStack getItemStack();

    @Shadow @Final private ItemStack stack;

    @Inject(method = "getNbt", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$areEqual(CallbackInfoReturnable<NbtCompound> cir) {
        if (CompatUtils.isServerSide(this.stack)) {
            cir.setReturnValue(CompatUtils.getBackingNbt(this.stack));
        }
    }
}
