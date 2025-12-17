package eu.pb4.polymer.resourcepack.extras.api.format.item.tint;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record FireworkTintSource(int defaultColor) implements ItemTintSource {
    public static final MapCodec<FireworkTintSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(FireworkTintSource::defaultColor)
    ).apply(instance, FireworkTintSource::new));

    public FireworkTintSource() {
        this(-7697782);
    }

    @Override
    public MapCodec<FireworkTintSource> codec() {
        return CODEC;
    }
}
