package eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record UseCycleProperty(float period) implements NumericProperty {
    public static final MapCodec<UseCycleProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("period", 1.0F).forGetter(UseCycleProperty::period)
    ).apply(instance, UseCycleProperty::new));

    @Override
    public MapCodec<UseCycleProperty> codec() {
        return CODEC;
    }
}
