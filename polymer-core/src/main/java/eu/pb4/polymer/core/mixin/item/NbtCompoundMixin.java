package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.interfaces.TypeAwareNbtCompound;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;

@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements TypeAwareNbtCompound {
    @Shadow
    private static void write(String key, NbtElement element, DataOutput output) throws IOException {
    }

    @Unique
    private NbtString polymerCore$type;

    @Override
    public void polymerCore$setType(NbtString value) {
        this.polymerCore$type = value;
    }

    @Override
    public NbtString polymerCore$getType() {
        return this.polymerCore$type;
    }
// todo
    @Inject(method = "write(Ljava/io/DataOutput;)V", at = @At("HEAD"))
    private void polymerCore$storePlayerContextedItemStack(DataOutput output, CallbackInfo ci, @Share("polymerCore:stack") LocalRef<Object> polymerStack) {
        /*if (this.polymerCore$type != null && PolymerCommonUtils.isServerNetworkingThread()) {
            var player = PolymerCommonUtils.getPlayerContextNoClient();
            if (this.polymerCore$type == TypeAwareNbtCompound.STACK_TYPE) {
                var stack = ItemStack.fromNbt((NbtCompound) (Object) this);
                if (player != null && stack != null && !stack.isEmpty()) {
                    polymerStack.set(PolymerItemUtils.getPolymerItemStack(stack, player));
                }
            } else if (this.polymerCore$type == TypeAwareNbtCompound.STATE_TYPE) {
                var stack = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), (NbtCompound) (Object) this);
                if (player != null && stack != null && !stack.isAir()) {
                    polymerStack.set(PolymerBlockUtils.getPolymerBlockState(stack, player));
                }
            }
        }*/
    }

    @WrapWithCondition(
            method = "write(Ljava/io/DataOutput;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;write(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;Ljava/io/DataOutput;)V")
    )
    private boolean polymerCore$ignoreIdAndTag(String key, NbtElement element, DataOutput output, @Share("polymerCore:stack") LocalRef<Object> polymerStack) {
        if (this.polymerCore$type != null && PolymerCommonUtils.isServerNetworkingThread()) {
            var stack = polymerStack.get();
            if (stack != null) {
                if (this.polymerCore$type == TypeAwareNbtCompound.STACK_TYPE) {
                    return !key.equals("id") && !key.equals("tag");

                } else if (this.polymerCore$type == TypeAwareNbtCompound.STATE_TYPE) {
                    return !key.equals("Name") && !key.equals("Properties");
                }
            }
        }

        return true;
    }
    @Inject(method = "write(Ljava/io/DataOutput;)V", at = @At(value = "INVOKE", target = "Ljava/io/DataOutput;writeByte(I)V", ordinal = 0))
    private void polymerCore$writeNbtIfMissing(DataOutput output, CallbackInfo ci, @Share("polymerCore:stack") LocalRef<Object> polymerStack) throws IOException {
        /*
        if (this.polymerCore$type != null && PolymerCommonUtils.isServerNetworkingThread()) {
            write(MARKER_KEY, this.polymerCore$type, output);
            if (this.polymerCore$type == TypeAwareNbtCompound.STACK_TYPE) {
                var stack = (ItemStack) polymerStack.get();
                if (stack != null) {
                    write("id", NbtString.of(Registries.ITEM.getId(stack.getItem()).toString()), output);
                    if (stack.hasNbt()) {
                        write("tag", stack.getNbt(), output);
                    }
                }
            } else if (this.polymerCore$type == TypeAwareNbtCompound.STATE_TYPE) {
                var stack = (BlockState) polymerStack.get();
                if (stack != null) {
                    write("Name", NbtString.of(Registries.BLOCK.getId(stack.getBlock()).toString()), output);
                    //noinspection unchecked
                    var props = (Collection<Property>) (Object) stack.getBlock().getStateManager().getProperties();
                    if (!props.isEmpty()) {
                        output.writeByte(NbtElement.COMPOUND_TYPE);
                        output.writeUTF("Properties");

                        for(var prop : props) {
                            output.writeByte(NbtElement.STRING_TYPE);
                            output.writeUTF(prop.getName());
                            //noinspection unchecked
                            output.writeUTF(prop.name(stack.get(prop)));
                        }
                        output.writeByte(0);
                    }
                }
            }
        }*/
    }

    @Inject(method = "copy()Lnet/minecraft/nbt/NbtCompound;", at = @At("RETURN"))
    private void polymerCore$copyStack(CallbackInfoReturnable<NbtCompound> cir) {
        ((TypeAwareNbtCompound) cir.getReturnValue()).polymerCore$setType(this.polymerCore$type);
    }
}
