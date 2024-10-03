package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Supplier;


@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyExpressionValue(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/tooltip/TooltipType;isAdvanced()Z"))
    private boolean removeAdvanced(boolean original, @Local(ordinal = 0, argsOnly = true) TooltipType type) {
        return original && !(type instanceof PolymerTooltipType);
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;lazyInitialized(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;"))
    private static Supplier<Codec<ItemStack>> patchCodec(Supplier<Codec<ItemStack>> codec) {
        return () -> codec.get().xmap(content -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var context = PacketContext.get();
                var lookup = context.getRegistryWrapperLookup() != null ? context .getRegistryWrapperLookup() : PolymerImplUtils.FALLBACK_LOOKUP;
                return PolymerItemUtils.getRealItemStack(content, lookup);
            }
            return content;
        }, content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThreadWithContext()) {
                var ctx = PacketContext.get();
                if (ctx.getPacketListener() == null) {
                    return content;
                }
                return PolymerItemUtils.getPolymerItemStack(content, ctx);
            }
            return content;
        });
    }
}
