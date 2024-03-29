package eu.pb4.polymer.core.mixin.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(HoverEvent.ItemStackContent.class)
public abstract class ItemStackContentMixin {
    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;withAlternative(Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/Codec;")
    )
    private static Codec<HoverEvent.ItemStackContent> patchCodec(Codec<HoverEvent.ItemStackContent> codec) {
        return codec.xmap(Function.identity(), content -> {
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var stack = content.asStack();
                return new HoverEvent.ItemStackContent(PolymerItemUtils.getPolymerItemStack(stack, PolymerCommonUtils.getPlayerContext()));
            }
            return content;
        });
    }
}
