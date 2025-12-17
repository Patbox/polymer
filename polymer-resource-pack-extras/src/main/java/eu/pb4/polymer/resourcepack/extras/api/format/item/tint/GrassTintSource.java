package eu.pb4.polymer.resourcepack.extras.api.format.item.tint;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record GrassTintSource(float temperature, float downfall) implements ItemTintSource {
    public static final MapCodec<GrassTintSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("temperature").forGetter(GrassTintSource::temperature),
            ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("downfall").forGetter(GrassTintSource::downfall)
    ).apply(instance, GrassTintSource::new));

    public GrassTintSource() {
        this(0.5F, 1.0F);
    }

    @Override
    public MapCodec<GrassTintSource> codec() {
        return CODEC;
    }
}
