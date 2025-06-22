package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;

public interface FontProvider {
    MapCodec<FontProvider> CODEC = MapCodec.assumeMapUnsafe(Codec.lazyInitialized(() -> FontProvider.TYPES.getCodec(Identifier.CODEC).dispatch(FontProvider::codec, Function.identity())));
    Codecs.IdMapper<Identifier, MapCodec<? extends FontProvider>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.ofVanilla("bitmap"), BitmapProvider.CODEC);
        m.put(Identifier.ofVanilla("space"), SpaceProvider.CODEC);
        m.put(Identifier.ofVanilla("ttf"), TTFProvider.CODEC);
        m.put(Identifier.ofVanilla("unihex"), UnihexProvider.CODEC);
        m.put(Identifier.ofVanilla("reference"), ReferenceProvider.CODEC);
    });

    MapCodec<? extends FontProvider> codec();


    interface Builder {
        FontProvider build();
    }

}
