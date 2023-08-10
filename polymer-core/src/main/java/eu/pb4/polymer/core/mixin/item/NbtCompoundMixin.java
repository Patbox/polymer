package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.ItemStackAwareNbtCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataOutput;

@SuppressWarnings("InvalidInjectorMethodSignature")
@Mixin(NbtCompound.class)
public class NbtCompoundMixin implements ItemStackAwareNbtCompound {
    @Unique
    private ItemStack polymerCore$stack;

    @Override
    public void polymerCore$setItemStack(ItemStack stack) {
        this.polymerCore$stack = stack;
    }


    @Inject(method = "write(Ljava/io/DataOutput;)V", at = @At("HEAD"))
    private void polymerCore$storePlayerContextedItemStack(DataOutput output, CallbackInfo ci, @Share("polymerCore:stack") LocalRef<ItemStack> polymerStack) {
        if (this.polymerCore$stack != null && PolymerCommonUtils.isNetworkingThread()) {
            var player = PolymerCommonUtils.getPlayerContextNoClient();
            if (player != null) {
                polymerStack.set(PolymerItemUtils.getPolymerItemStack(this.polymerCore$stack, player));
            }
        }
    }

    @ModifyArg(method = "write(Ljava/io/DataOutput;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;write(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;Ljava/io/DataOutput;)V"))
    private NbtElement polymerCore$swapNbt(NbtElement nbtElement, @Local(ordinal = 0) String key, @Share("polymerCore:stack") LocalRef<ItemStack> polymerStack) {
        if (PolymerImpl.ITEMSTACK_NBT_HACK && this.polymerCore$stack != null && PolymerCommonUtils.isNetworkingThread()) {
            if (key.equals("id") && nbtElement.getType() == NbtElement.STRING_TYPE) {
                var stack = polymerStack.get();
                return stack != null ? NbtString.of(Registries.ITEM.getId(stack.getItem()).toString()) : nbtElement;
            } else if (key.equals("tag") && nbtElement.getType() == NbtElement.COMPOUND_TYPE) {
                var stack = polymerStack.get();
                return stack != null ? stack.getOrCreateNbt() : nbtElement;
            }
        }

        return nbtElement;
    }

    @Inject(method = "copy()Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private void polymerCore$copyStack(CallbackInfoReturnable<NbtCompound> cir) {
        ((ItemStackAwareNbtCompound) cir.getReturnValue()).polymerCore$setItemStack(this.polymerCore$stack);
    }
}
