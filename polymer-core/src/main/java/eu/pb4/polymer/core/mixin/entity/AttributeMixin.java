package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

@Mixin(Attribute.class)
public abstract class AttributeMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;holderByNameCodec()Lcom/mojang/serialization/Codec;"))
    private static Codec<Holder<Attribute>> patchCodec(Codec<Holder<Attribute>> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && PolymerEntityUtils.isPolymerEntityAttribute(content)) {
                return Attributes.SPAWN_REINFORCEMENTS_CHANCE;
            }
            return content;
        });
    }
}
