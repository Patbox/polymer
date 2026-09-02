package eu.pb4.polymer.resourcepack.extras.api.format.item.tint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import java.util.function.Function;

public interface ItemTintSource {
	ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>> TYPES = new LazyIdMapper<>(m -> {
		m.put(Identifier.withDefaultNamespace("custom_model_data"), CustomModelDataTintSource.CODEC);
		m.put(Identifier.withDefaultNamespace("constant"), ConstantTintSource.CODEC);
		m.put(Identifier.withDefaultNamespace("dye"), DyeTintSource.CODEC);
		m.put(Identifier.withDefaultNamespace("grass"), GrassTintSource.CODEC);
		m.put(Identifier.withDefaultNamespace("firework"), FireworkTintSource.CODEC);
		m.put(Identifier.withDefaultNamespace("potion"), PotionTintSource.CODEC);
		m.put(Identifier.withDefaultNamespace("team"), TeamTintSource.CODEC);
	});
	Codec<ItemTintSource> CODEC = TYPES.codec(Identifier.CODEC).dispatch(ItemTintSource::codec, Function.identity());


	MapCodec<? extends ItemTintSource> codec();
}
