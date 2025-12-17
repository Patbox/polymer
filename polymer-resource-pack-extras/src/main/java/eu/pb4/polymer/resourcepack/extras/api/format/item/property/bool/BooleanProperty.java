package eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import java.util.function.Function;

public interface BooleanProperty {
	ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends BooleanProperty>> TYPES = new LazyIdMapper<>(m -> {
		m.put(Identifier.parse("custom_model_data"), CustomModelDataFlagProperty.CODEC);
		m.put(Identifier.parse("using_item"), UsingItemProperty.CODEC);
		m.put(Identifier.parse("broken"), BrokenProperty.CODEC);
		m.put(Identifier.parse("damaged"), DamagedProperty.CODEC);
		m.put(Identifier.parse("fishing_rod/cast"), FishingRodCastProperty.CODEC);
		m.put(Identifier.parse("has_component"), HasComponentProperty.CODEC);
		m.put(Identifier.parse("bundle/has_selected_item"), BundleHasSelectedItemProperty.CODEC);
		m.put(Identifier.parse("selected"), SelectedProperty.CODEC);
		m.put(Identifier.parse("carried"), CarriedProperty.CODEC);
		m.put(Identifier.parse("extended_view"), ExtendedViewProperty.CODEC);
		m.put(Identifier.parse("keybind_down"), KeybindDownProperty.CODEC);
		m.put(Identifier.parse("view_entity"), ViewEntityProperty.CODEC);
		m.put(Identifier.parse("component"), ComponentBooleanProperty.CODEC);
	});
	MapCodec<BooleanProperty> CODEC = TYPES.codec(Identifier.CODEC).dispatchMap("property", BooleanProperty::codec, Function.identity());

	MapCodec<? extends BooleanProperty> codec();
}
