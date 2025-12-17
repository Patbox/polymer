package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

public record ContextEntityTypeProperty() implements SelectProperty<ResourceKey<EntityType<?>>> {
	public static final Type<ContextEntityTypeProperty, ResourceKey<EntityType<?>>> TYPE = new Type<>(
		MapCodec.unit(new ContextEntityTypeProperty()), ResourceKey.codec(Registries.ENTITY_TYPE)
	);

	@Override
	public Type<ContextEntityTypeProperty, ResourceKey<EntityType<?>>> type() {
		return TYPE;
	}
}
