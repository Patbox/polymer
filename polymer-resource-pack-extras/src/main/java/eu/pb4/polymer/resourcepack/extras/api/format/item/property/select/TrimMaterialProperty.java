package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public record TrimMaterialProperty() implements SelectProperty<ResourceKey<TrimMaterial>> {
    public static final Type<TrimMaterialProperty, ResourceKey<TrimMaterial>> TYPE = new Type<>(
            MapCodec.unit(new TrimMaterialProperty()), ResourceKey.codec(Registries.TRIM_MATERIAL)
    );

    @Override
    public Type<TrimMaterialProperty, ResourceKey<TrimMaterial>> type() {
        return TYPE;
    }
}
