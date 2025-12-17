package eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record CustomModelDataFlagProperty(int index) implements BooleanProperty {
    public static final MapCodec<CustomModelDataFlagProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(CustomModelDataFlagProperty::index)
            ).apply(instance, CustomModelDataFlagProperty::new)
    );

    @Override
    public MapCodec<CustomModelDataFlagProperty> codec() {
        return CODEC;
    }
}
