package eu.pb4.polymer.core.mixin.tag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.impl.other.PolymerTagHacks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TagKey.class)
public class TagKeyMixin {
    /*@ModifyReturnValue(method = {"codec", "hashedCodec"}, at = @At("RETURN"))
    private static Codec<TagKey<?>> remapFakeEntries(Codec<TagKey<?>> original) {
        return original.xmap(content -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return PolymerTagHacks.FAKE_TO_REAL.getOrDefault(content, content);
            }
            return content;
        }, content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return PolymerTagHacks.REAL_TO_FAKE.getOrDefault(content, content);
            }
            return content;
        });
    }

    @ModifyReturnValue(method = {"streamCodec"}, at = @At("RETURN"))
    private static StreamCodec<ByteBuf, TagKey<?>> remapFakeEntries2(StreamCodec<ByteBuf, TagKey<?>> original) {
        return original.map(content -> { // Decode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return PolymerTagHacks.FAKE_TO_REAL.getOrDefault(content, content);
            }
            return content;
        }, content -> { // Encode
            if (PolymerCommonUtils.isServerNetworkingThread()) {
                return PolymerTagHacks.REAL_TO_FAKE.getOrDefault(content, content);
            }
            return content;
        });
    }*/
}
