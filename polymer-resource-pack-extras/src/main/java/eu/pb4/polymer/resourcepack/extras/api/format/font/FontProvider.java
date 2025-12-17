package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import java.util.function.Function;
import net.minecraft.util.ExtraCodecs;

public interface FontProvider {
    MapCodec<FontProvider> CODEC = MapCodec.assumeMapUnsafe(Codec.lazyInitialized(() -> FontProvider.TYPES.codec(Codec.STRING).dispatch(FontProvider::codec, Function.identity())));
    ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends FontProvider>> TYPES = new LazyIdMapper<>(m -> {
        m.put("bitmap", BitmapProvider.CODEC);
        m.put("space", SpaceProvider.CODEC);
        m.put("ttf", TTFProvider.CODEC);
        m.put("unihex", UnihexProvider.CODEC);
        m.put("reference", ReferenceProvider.CODEC);
    });

    MapCodec<? extends FontProvider> codec();


    interface Builder {
        FontProvider build();
    }

}
