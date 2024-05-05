package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(TradedItem.class)
public class TradedItemMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, TradedItem> polymerifyTheStack(PacketCodec<RegistryByteBuf, TradedItem> original) {
        return new TransformingPacketCodec<>(original, (buf, tradedItem) -> {
            var stack = PolymerItemUtils.getPolymerItemStack(tradedItem.itemStack(), buf.getRegistryManager(), PacketContext.get().getPlayer());
            return new TradedItem(stack.getItem().getRegistryEntry(), stack.getCount(), ComponentPredicate.of(stack.getComponents()));
        }, (buf, tradedItem) -> {
            var stack = PolymerItemUtils.getRealItemStack(tradedItem.itemStack(), buf.getRegistryManager());
            return new TradedItem(stack.getItem().getRegistryEntry(), stack.getCount(), ComponentPredicate.of(stack.getComponents()));
        });
    }
}
