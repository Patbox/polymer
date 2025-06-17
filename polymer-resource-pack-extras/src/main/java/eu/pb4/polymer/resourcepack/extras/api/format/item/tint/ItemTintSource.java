package eu.pb4.polymer.resourcepack.extras.api.format.item.tint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;

public interface ItemTintSource {
	Codecs.IdMapper<Identifier, MapCodec<? extends ItemTintSource>> TYPES = new LazyIdMapper<>(m -> {
		m.put(Identifier.ofVanilla("custom_model_data"), CustomModelDataTintSource.CODEC);
		m.put(Identifier.ofVanilla("constant"), ConstantTintSource.CODEC);
		m.put(Identifier.ofVanilla("dye"), DyeTintSource.CODEC);
		m.put(Identifier.ofVanilla("grass"), GrassTintSource.CODEC);
		m.put(Identifier.ofVanilla("firework"), FireworkTintSource.CODEC);
		m.put(Identifier.ofVanilla("potion"), PotionTintSource.CODEC);
		m.put(Identifier.ofVanilla("map_color"), MapColorTintSource.CODEC);
		m.put(Identifier.ofVanilla("team"), TeamTintSource.CODEC);
	});
	Codec<ItemTintSource> CODEC = TYPES.getCodec(Identifier.CODEC).dispatch(ItemTintSource::codec, Function.identity());


	MapCodec<? extends ItemTintSource> codec();
}
