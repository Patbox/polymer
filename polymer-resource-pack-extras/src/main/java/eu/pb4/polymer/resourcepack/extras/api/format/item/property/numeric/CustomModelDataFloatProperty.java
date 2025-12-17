package eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record CustomModelDataFloatProperty(int index) implements NumericProperty {
	public static final MapCodec<CustomModelDataFloatProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(CustomModelDataFloatProperty::index)
					).apply(instance, CustomModelDataFloatProperty::new)
	);

	@Override
	public MapCodec<CustomModelDataFloatProperty> codec() {
		return CODEC;
	}
}
