package eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import java.util.function.Function;

public interface NumericProperty {
    ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends NumericProperty>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.parse("custom_model_data"), CustomModelDataFloatProperty.CODEC);
        m.put(Identifier.parse("bundle/fullness"), BundleFullnessProperty.CODEC);
        m.put(Identifier.parse("damage"), DamageProperty.CODEC);
        m.put(Identifier.parse("cooldown"), CooldownProperty.CODEC);
        m.put(Identifier.parse("time"), TimeProperty.CODEC);
        m.put(Identifier.parse("compass"), CompassProperty.CODEC);
        m.put(Identifier.parse("crossbow/pull"), CrossbowPullProperty.CODEC);
        m.put(Identifier.parse("use_cycle"), UseCycleProperty.CODEC);
        m.put(Identifier.parse("use_duration"), UseDurationProperty.CODEC);
        m.put(Identifier.parse("count"), CountProperty.CODEC);
    });
    MapCodec<NumericProperty> CODEC = TYPES.codec(Identifier.CODEC).dispatchMap("property", NumericProperty::codec, Function.identity());

    MapCodec<? extends NumericProperty> codec();
}
