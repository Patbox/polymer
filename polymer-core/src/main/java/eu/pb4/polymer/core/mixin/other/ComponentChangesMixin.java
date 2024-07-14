package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.impl.TransformingComponent;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Function;

@Mixin(ComponentChanges.class)
public class ComponentChangesMixin {
    @Mutable
    @Shadow @Final public static PacketCodec<RegistryByteBuf, ComponentChanges> PACKET_CODEC;

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<ComponentChanges> patchCodec(Codec<ComponentChanges> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return transformContent(content);
            }
            return content;
        });
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void patchNetCodec(CallbackInfo ci) {
        PACKET_CODEC = TransformingPacketCodec.encodeOnly(PACKET_CODEC, ((byteBuf, content) -> transformContent(content)));
    }

    @Unique
    private static ComponentChanges transformContent(ComponentChanges content) {
        var player = PacketContext.get();
        var builder = ComponentChanges.builder();
        for (var entry : content.entrySet()) {
            if (!PolymerComponent.canSync(entry.getKey(), entry.getValue().orElse(null), player)) {
                continue;
            } else if (entry.getValue().isPresent() && entry.getValue().get() instanceof TransformingComponent t) {
                //noinspection unchecked
                builder.add((ComponentType<Object>) entry.getKey(), t.polymer$getTransformed(player));
            }

            if (entry.getValue().isPresent()) {
                //noinspection unchecked
                builder.add((ComponentType<Object>) entry.getKey(), entry.getValue().get());
            } else {
                builder.remove(entry.getKey());
            }
        }
        return builder.build();
    }
}
