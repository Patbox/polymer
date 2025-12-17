package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record CustomModelDataStringProperty(int index) implements SelectProperty<String> {
    public static final Type<CustomModelDataStringProperty, String> TYPE = new Type<>(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(CustomModelDataStringProperty::index)
            ).apply(instance, CustomModelDataStringProperty::new)),
            Codec.STRING
    );

    @Override
    public Type<CustomModelDataStringProperty, String> type() {
        return TYPE;
    }
}
