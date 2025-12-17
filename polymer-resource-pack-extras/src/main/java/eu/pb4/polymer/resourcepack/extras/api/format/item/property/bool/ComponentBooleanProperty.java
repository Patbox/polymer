package eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public record ComponentBooleanProperty(DataComponentPredicate.Single<?> predicate) implements BooleanProperty {
    public static final MapCodec<ComponentBooleanProperty> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(DataComponentPredicate.singleCodec("predicate").forGetter(ComponentBooleanProperty::predicate)).apply(instance, ComponentBooleanProperty::new);
    });
    public MapCodec<ComponentBooleanProperty> codec() {
        return CODEC;
    }
}