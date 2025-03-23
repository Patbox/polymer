package eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.predicate.component.ComponentPredicate;

public record ComponentBooleanProperty(ComponentPredicate.Typed<?> predicate) implements BooleanProperty {
    public static final MapCodec<ComponentBooleanProperty> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(ComponentPredicate.createCodec("predicate").forGetter(ComponentBooleanProperty::predicate)).apply(instance, ComponentBooleanProperty::new);
    });
    public MapCodec<ComponentBooleanProperty> codec() {
        return CODEC;
    }
}