package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.CrossbowItem;

public record ChargeTypeProperty() implements SelectProperty<CrossbowItem.ChargeType> {
	public static final Type<ChargeTypeProperty, CrossbowItem.ChargeType> TYPE = new Type<>(
		MapCodec.unit(new ChargeTypeProperty()), CrossbowItem.ChargeType.CODEC
	);

	@Override
	public Type<ChargeTypeProperty, CrossbowItem.ChargeType> type() {
		return TYPE;
	}
}
