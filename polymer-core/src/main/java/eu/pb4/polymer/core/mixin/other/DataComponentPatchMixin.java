package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.function.Function;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@Mixin(DataComponentPatch.class)
public class DataComponentPatchMixin {
    @Mutable
    @Shadow @Final public static StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> STREAM_CODEC;

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<DataComponentPatch> patchCodec(Codec<DataComponentPatch> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return transformContent(content);
            }
            return content;
        });
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void patchNetCodec(CallbackInfo ci) {
        STREAM_CODEC = TransformingPacketCodec.encodeOnly(STREAM_CODEC, ((byteBuf, content) -> transformContent(content)));
    }

    @Unique
    private static DataComponentPatch transformContent(DataComponentPatch content) {
        var player = PacketContext.get();
        var builder = DataComponentPatch.builder();
        
        var split = content.split();
        for (var key : split.added().keySet()) {
            var value = split.added().get(key);
            if (!PolymerComponent.canSync(key, value, player)) {
                continue;
            } else if (value instanceof TransformingComponent t) {
                //noinspection unchecked
                builder.set((DataComponentType<Object>) key, t.polymer$getTransformed(player));
            } else {
                assert value != null;
                //noinspection unchecked
                builder.set((DataComponentType<Object>) key, value);
            }
        }

        for (var key : split.removed()) {
            if (PolymerComponent.canSync(key, null, player)) {
                builder.remove(key);
            }
        }

        return builder.build();
    }
}
