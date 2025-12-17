package eu.pb4.polymer.resourcepack.extras.api.format.item.tint;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.component.MapItemColor;

public record MapColorTintSource(int defaultColor) implements ItemTintSource {
    public static final MapCodec<MapColorTintSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(MapColorTintSource::defaultColor)
    ).apply(instance, MapColorTintSource::new));

    public MapColorTintSource() {
        this(MapItemColor.DEFAULT.rgb());
    }

    @Override
    public MapCodec<MapColorTintSource> codec() {
        return CODEC;
    }
}
