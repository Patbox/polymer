package eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;

public interface NumericProperty {
    Codecs.IdMapper<Identifier, MapCodec<? extends NumericProperty>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.of("custom_model_data"), CustomModelDataFloatProperty.CODEC);
        m.put(Identifier.of("bundle/fullness"), BundleFullnessProperty.CODEC);
        m.put(Identifier.of("damage"), DamageProperty.CODEC);
        m.put(Identifier.of("cooldown"), CooldownProperty.CODEC);
        m.put(Identifier.of("time"), TimeProperty.CODEC);
        m.put(Identifier.of("compass"), CompassProperty.CODEC);
        m.put(Identifier.of("crossbow/pull"), CrossbowPullProperty.CODEC);
        m.put(Identifier.of("use_cycle"), UseCycleProperty.CODEC);
        m.put(Identifier.of("use_duration"), UseDurationProperty.CODEC);
        m.put(Identifier.of("count"), CountProperty.CODEC);
    });
    MapCodec<NumericProperty> CODEC = TYPES.getCodec(Identifier.CODEC).dispatchMap("property", NumericProperty::codec, Function.identity());

    MapCodec<? extends NumericProperty> codec();
}
