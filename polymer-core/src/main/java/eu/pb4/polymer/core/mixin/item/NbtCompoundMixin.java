package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.interfaces.ItemStackAwareNbtCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements ItemStackAwareNbtCompound {
    @Shadow
    private static void write(String key, NbtElement element, DataOutput output) throws IOException {
    }

    @Unique
    private boolean polymerCore$stack;

    @Override
    public void polymerCore$setItemStack(boolean value) {
        this.polymerCore$stack = value;
    }

    @Override
    public boolean polymerCore$getItemStack() {
        return this.polymerCore$stack;
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

    @WrapWithCondition(
            method = "write(Ljava/io/DataOutput;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;write(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;Ljava/io/DataOutput;)V")
    )
    private boolean polymerCore$ignoreIdAndTag(String key, NbtElement element, DataOutput output, @Share("polymerCore:stack") LocalRef<ItemStack> polymerStack) {
        if (this.polymerCore$stack && PolymerCommonUtils.isNetworkingThread()) {
            var stack = polymerStack.get();
            if (stack != null) {
                return !key.equals("id") && !key.equals("tag");
            }
        }

        return true;
    }
    @Inject(method = "write(Ljava/io/DataOutput;)V", at = @At(value = "INVOKE", target = "Ljava/io/DataOutput;writeByte(I)V", ordinal = 0))
    private void polymerCore$writeNbtIfMissing(DataOutput output, CallbackInfo ci, @Share("polymerCore:stack") LocalRef<ItemStack> polymerStack) throws IOException {
        if (this.polymerCore$stack && PolymerCommonUtils.isNetworkingThread()) {
            var stack = polymerStack.get();
            if (stack != null) {
                write(MARKER_KEY, MARKER_VALUE, output);
                write("id", NbtString.of(Registries.ITEM.getId(stack.getItem()).toString()), output);
                if (stack.hasNbt()) {
                    write("tag", stack.getNbt(), output);
                }
            }
        }
    }

    @Inject(method = "copy()Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private void polymerCore$copyStack(CallbackInfoReturnable<NbtCompound> cir) {
        ((ItemStackAwareNbtCompound) cir.getReturnValue()).polymerCore$setItemStack(this.polymerCore$stack);
    }
}
