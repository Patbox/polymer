package eu.pb4.polymer.core.mixin.tag;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.impl.other.PolymerTagHacks;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/network/codec/ByteBufCodecs$31", priority = 500)
public abstract class ByteBufCodecsHolderSetMixin {
    /*@ModifyReceiver(method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/core/HolderSet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagKey;location()Lnet/minecraft/resources/Identifier;"))
    private TagKey<?> swapTagKeysForEncoding(TagKey instance) {
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            return PolymerTagHacks.REAL_TO_FAKE.getOrDefault(instance, instance);
        }
        return instance;
    }


    @ModifyExpressionValue(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/core/HolderSet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagKey;create(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/Identifier;)Lnet/minecraft/tags/TagKey;"))
    private TagKey<?> swapTagKeysForDecoding(TagKey instance) {
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            return PolymerTagHacks.FAKE_TO_REAL.getOrDefault(instance, instance);
        }
        return instance;
    }*/
}