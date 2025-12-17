package eu.pb4.polymer.resourcepack.extras.api.format.atlas;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import java.util.List;
import java.util.function.Function;

public interface AtlasSource {
    Codec<AtlasSource> CODEC = Codec.lazyInitialized(() -> AtlasSource.TYPES.codec(Identifier.CODEC).dispatch(AtlasSource::codec, Function.identity()));
    ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends AtlasSource>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.withDefaultNamespace("single"), SingleAtlasSource.CODEC);
        m.put(Identifier.withDefaultNamespace("directory"), DirectoryAtlasSource.CODEC);
        m.put(Identifier.withDefaultNamespace("filter"), FilterAtlasSource.CODEC);
        m.put(Identifier.withDefaultNamespace("unstitch"), UnstitchAtlasSource.CODEC);
        m.put(Identifier.withDefaultNamespace("paletted_permutations"), PalettedPermutationsAtlasSource.CODEC);
    });

    MapCodec<? extends AtlasSource> codec();
}
