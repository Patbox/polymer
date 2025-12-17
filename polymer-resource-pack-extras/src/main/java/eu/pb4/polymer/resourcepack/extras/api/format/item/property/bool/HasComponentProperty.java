package eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public record HasComponentProperty(DataComponentType<?> componentType, boolean ignoreDefault) implements BooleanProperty {
    public static final MapCodec<HasComponentProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec().fieldOf("component").forGetter(HasComponentProperty::componentType),
                    Codec.BOOL.optionalFieldOf("ignore_default", Boolean.valueOf(false)).forGetter(HasComponentProperty::ignoreDefault)
            ).apply(instance, HasComponentProperty::new)
    );

    @Override
    public MapCodec<HasComponentProperty> codec() {
        return CODEC;
    }
}
