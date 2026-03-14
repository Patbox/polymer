package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.other.PolymerParticleType;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.function.Function;

@Mixin(ParticleTypes.class)
public class ParticleTypesMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static Codec<ParticleOptions> patchCodec(Codec<ParticleOptions> codec) {
        return codec.xmap(Function.identity(), content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && PolymerParticleType.getOverlay(content.getType()) instanceof PolymerParticleType<?> type) {
                //noinspection unchecked
                return ((PolymerParticleType<ParticleOptions>) type).getPolymerParticleReplacement(content, PacketContext.get());
            }
            return content;
        });
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ParticleOptions> patchStreamCodec(StreamCodec<RegistryFriendlyByteBuf, ParticleOptions> codec) {
        return TransformingPacketCodec.encodeOnly(codec, (buf, content) -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread() && PolymerParticleType.getOverlay(content.getType()) instanceof PolymerParticleType<?> type) {
                //noinspection unchecked
                return ((PolymerParticleType<ParticleOptions>) type).getPolymerParticleReplacement(content, PacketContext.get());
            }
            return content;
        });
    }
}
