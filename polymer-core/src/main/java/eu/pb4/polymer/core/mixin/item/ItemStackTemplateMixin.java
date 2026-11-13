package eu.pb4.polymer.core.mixin.item;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplPacketKeys;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;


@Mixin(ItemStackTemplate.class)
public class ItemStackTemplateMixin {
    @Unique
    private static final Supplier<ItemStackTemplate> FAKE_EMPTY = Suppliers.memoize(() -> new ItemStackTemplate(Items.BARRIER, DataComponentPatch.builder()
            .set(DataComponents.ITEM_NAME, Component.translatable("item.minecraft.bundle.empty").withColor(TextColor.RED))
            .build()));

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;mapCodec(Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"))
    private static MapCodec<ItemStackTemplate> patchCodec(MapCodec<ItemStackTemplate> mapCodec) {
        return mapCodec.xmap(content -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var context = PacketContext.orElseThrow();
                var lookup = context.orElse(PacketContext.REGISTRY_ACCESS, PolymerImplUtils.FALLBACK_LOOKUP);
                return fromPossiblyEmpty(PolymerItemUtils.getRealItemStack(createUnsafe(content), context, lookup));
            }
            return content;
        }, content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                var ctx = PacketContext.orElseThrow();
                return fromPossiblyEmpty(PolymerItemUtils.getPolymerItemStack(createUnsafe(content), ctx, ctx.orElse(PacketContext.REGISTRY_ACCESS, PolymerImplUtils.FALLBACK_LOOKUP)));
            }
            return content;
        });
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> patchPacketCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStackTemplate> streamCodec) {
        return new TransformingPacketCodec<>(streamCodec, (buf, content) -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return fromPossiblyEmpty(PolymerItemUtils.getPolymerItemStack(createUnsafe(content), PacketContext.orElseThrow(), buf.registryAccess()));
            }
            return content;
        }, (buf, content) -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return fromPossiblyEmpty(PolymerItemUtils.getRealItemStack(createUnsafe(content), PacketContext.orElseThrow(), buf.registryAccess()));
            }
            return content;
        });
    }
    
    private static ItemStack createUnsafe(ItemStackTemplate template) {
        return new ItemStack(template.item(), template.count(), template.components());
    }

    private static ItemStackTemplate fromPossiblyEmpty(ItemStack stack) {
        return stack.isEmpty() ? FAKE_EMPTY.get() : ItemStackTemplate.fromNonEmptyStack(stack);
    }
}
