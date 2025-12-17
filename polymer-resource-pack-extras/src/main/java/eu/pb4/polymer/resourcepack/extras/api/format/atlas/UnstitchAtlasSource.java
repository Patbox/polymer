package eu.pb4.polymer.resourcepack.extras.api.format.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record UnstitchAtlasSource(Identifier resource, List<Region> regions, double divisorX,
                                  double divisorY) implements AtlasSource {
    public static final MapCodec<UnstitchAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("resource").forGetter(UnstitchAtlasSource::resource),
            ExtraCodecs.nonEmptyList(Region.CODEC.listOf()).fieldOf("regions").forGetter(UnstitchAtlasSource::regions),
            Codec.DOUBLE.optionalFieldOf("divisor_x", 1.0).forGetter(UnstitchAtlasSource::divisorX),
            Codec.DOUBLE.optionalFieldOf("divisor_y", 1.0).forGetter(UnstitchAtlasSource::divisorY)
    ).apply(instance, UnstitchAtlasSource::new));

    public UnstitchAtlasSource(Identifier resource, List<Region> regions) {
        this(resource, regions, 1, 1);
    }

    @Override
    public MapCodec<? extends AtlasSource> codec() {
        return CODEC;
    }

    public record Region(Identifier sprite, double x, double y, double width, double height) {
        public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("sprite").forGetter(Region::sprite),
                Codec.DOUBLE.fieldOf("x").forGetter(Region::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Region::y),
                Codec.DOUBLE.fieldOf("width").forGetter(Region::width),
                Codec.DOUBLE.fieldOf("height").forGetter(Region::height)
        ).apply(instance, Region::new));
    }
}
