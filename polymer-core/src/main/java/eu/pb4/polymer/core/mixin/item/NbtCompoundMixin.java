package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
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
    private boolean polymerCore$stack;

    @Override
    public void polymerCore$setItemStack(boolean value) {
        this.polymerCore$stack = value;
    }


    @Inject(method = "write(Ljava/io/DataOutput;)V", at = @At("HEAD"))
    private void polymerCore$storePlayerContextedItemStack(DataOutput output, CallbackInfo ci, @Share("polymerCore:stack") LocalRef<ItemStack> polymerStack) {
        if (this.polymerCore$stack && PolymerCommonUtils.isNetworkingThread()) {
            var player = PolymerCommonUtils.getPlayerContextNoClient();
            var stack = ItemStack.fromNbt((NbtCompound) (Object) this);
            if (player != null && stack != null && !stack.isEmpty()) {
                polymerStack.set(PolymerItemUtils.getPolymerItemStack(stack, player));
            }
        }
    }

    @ModifyArg(method = "write(Ljava/io/DataOutput;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;write(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;Ljava/io/DataOutput;)V"))
    private NbtElement polymerCore$swapNbt(NbtElement nbtElement, @Local(ordinal = 0) String key, @Share("polymerCore:stack") LocalRef<ItemStack> polymerStack) {
        if (this.polymerCore$stack && PolymerCommonUtils.isNetworkingThread()) {
            var stack = polymerStack.get();
            if (stack != null) {
                if (key.equals("id") && nbtElement.getType() == NbtElement.STRING_TYPE) {
                    return NbtString.of(Registries.ITEM.getId(stack.getItem()).toString());
                } else if (key.equals("tag") && nbtElement.getType() == NbtElement.COMPOUND_TYPE) {
                    return stack.getOrCreateNbt();
                }
            }
        }

        return nbtElement;
    }

    @Inject(method = "copy()Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private void polymerCore$copyStack(CallbackInfoReturnable<NbtCompound> cir) {
        ((ItemStackAwareNbtCompound) cir.getReturnValue()).polymerCore$setItemStack(this.polymerCore$stack);
    }
}
