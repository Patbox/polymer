package eu.pb4.polymer.core.mixin.client.compat;

import dev.emi.emi.api.stack.ItemEmiStack;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Pseudo
@Mixin(ItemEmiStack.class)
public abstract class emi_ItemEmiStack2Mixin {
    @Shadow public abstract ItemStack getItemStack();

    @Inject(method = "getKey", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$getKey(CallbackInfoReturnable<Object> cir) {
        if (CompatUtils.isServerSide(this.getItemStack())) {
            cir.setReturnValue(CompatUtils.getKey(this.getItemStack()));
        }
    }

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$getId(CallbackInfoReturnable<Identifier> cir) {
        var x = PolymerItemUtils.getServerIdentifier(this.getItemStack());
        if (x != null) {
            cir.setReturnValue(x);
        }
    }

    @Inject(method = "getNbt", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$getNbt(CallbackInfoReturnable<NbtCompound> cir) {
        if (CompatUtils.isServerSide(this.getItemStack())) {
            cir.setReturnValue(CompatUtils.getBackingNbt(this.getItemStack()));
        }
    }
}
