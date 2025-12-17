package eu.pb4.polymer.resourcepack.extras.api.format.atlas;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.mixin.accessors.IdentifierPatternAccessor;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.util.IdentifierPattern;

public record FilterAtlasSource(IdentifierPattern pattern) implements AtlasSource {
    public static final MapCodec<FilterAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    IdentifierPattern.CODEC.fieldOf("pattern").forGetter(FilterAtlasSource::pattern))
            .apply(instance, FilterAtlasSource::new));

    public FilterAtlasSource(Optional<Pattern> namespace, Optional<Pattern> path) {
        this(IdentifierPatternAccessor.create(namespace, path));
    }

    public FilterAtlasSource(Pattern namespace, Pattern path) {
        this(Optional.ofNullable(namespace), Optional.ofNullable(path));
    }
    @Override
    public MapCodec<? extends AtlasSource> codec() {
        return CODEC;
    }
}
