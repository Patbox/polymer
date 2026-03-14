package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import eu.pb4.polymer.core.impl.other.ComponentChangesMap;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(ItemCost.class)
public class ItemCostMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ItemCost> polymerifyTheStack(StreamCodec<RegistryFriendlyByteBuf, ItemCost> original) {
        return new TransformingPacketCodec<>(original, (buf, tradedItem) -> {
            var input = tradedItem.itemStack();
            var stack = PolymerItemUtils.getPolymerItemStack(input, PacketContext.get(), buf.registryAccess());
            return stack != input ? new ItemCost(stack.typeHolder(), stack.getCount(), DataComponentExactPredicate.allOf(new ComponentChangesMap(stack.getComponentsPatch()))) : tradedItem;
        }, (buf, tradedItem) -> {
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var input = tradedItem.itemStack();
                var stack = PolymerItemUtils.getRealItemStack(input, buf.registryAccess());
                return stack != input ? new ItemCost(stack.typeHolder(), stack.getCount(), DataComponentExactPredicate.allOf(new ComponentChangesMap(stack.getComponentsPatch()))) : tradedItem;
            }
            return tradedItem;
        });
    }
}
