package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplPacketKeys;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ItemStackTemplate.class)
public class ItemStackTemplateMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<ItemStackTemplate> patchCodec(MapCodec<ItemStackTemplate> mapCodec) {
        return mapCodec.xmap(content -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var context = PacketContext.orElseThrow();
                var lookup = context.orElse(CommonImplPacketKeys.HOLDER_LOOKUP, PolymerImplUtils.FALLBACK_LOOKUP);
                return ItemStackTemplate.fromNonEmptyStack(PolymerItemUtils.getRealItemStack(content.create(), lookup));
            }
            return content;
        }, content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var ctx = PacketContext.orElseThrow();
                return ItemStackTemplate.fromNonEmptyStack(PolymerItemUtils.getPolymerItemStack(content.create(), ctx, ctx.orElse(CommonImplPacketKeys.HOLDER_LOOKUP, PolymerImplUtils.FALLBACK_LOOKUP)));
            }
            return content;
        });
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> patchPacketCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> streamCodec) {
        return new TransformingPacketCodec<>(streamCodec, (buf, content) -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return ItemStackTemplate.fromNonEmptyStack(PolymerItemUtils.getPolymerItemStack(content.create(), PacketContext.orElseThrow(), buf.registryAccess()));
            }
            return content;
        }, (buf, content) -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return ItemStackTemplate.fromNonEmptyStack(PolymerItemUtils.getRealItemStack(content.create(), buf.registryAccess()));
            }
            return content;
        });
    }
}
