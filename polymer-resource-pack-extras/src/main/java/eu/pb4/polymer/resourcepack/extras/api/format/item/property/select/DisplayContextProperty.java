package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemDisplayContext;

public record DisplayContextProperty() implements SelectProperty<ItemDisplayContext> {
    public static final Type<DisplayContextProperty, ItemDisplayContext> TYPE = new Type<>(
            MapCodec.unit(new DisplayContextProperty()), ItemDisplayContext.CODEC
    );

    @Override
    public Type<DisplayContextProperty, ItemDisplayContext> type() {
        return TYPE;
    }
}
