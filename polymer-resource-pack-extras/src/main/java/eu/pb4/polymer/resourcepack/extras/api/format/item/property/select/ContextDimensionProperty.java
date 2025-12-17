package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ContextDimensionProperty() implements SelectProperty<ResourceKey<Level>> {
	public static final Type<ContextDimensionProperty, ResourceKey<Level>> TYPE = new Type<>(
		MapCodec.unit(new ContextDimensionProperty()), ResourceKey.codec(Registries.DIMENSION)
	);

	@Override
	public Type<ContextDimensionProperty, ResourceKey<Level>> type() {
		return TYPE;
	}
}
