package eu.pb4.polymer.core.api.other;

import com.mojang.serialization.*;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;

import java.util.List;
import java.util.stream.Stream;

public class PolymerMapCodec<T> extends MapCodec<T> implements PolymerObject {
    private final MapCodec<T> selfCodec;
    private final MapCodec<Object> fallbackCodec;
    private final Object fallbackValue;

    public <K> PolymerMapCodec(MapCodec<T> selfCodec, MapCodec<K> fallbackCodec, K fallbackValue) {
        this.selfCodec = selfCodec;
        //noinspection unchecked
        this.fallbackCodec = (MapCodec<Object>) fallbackCodec;
        this.fallbackValue = fallbackValue;
    }

    public static <T extends EnchantmentValueEffect> MapCodec<T> ofEnchantmentValueEffect(MapCodec<T> codec) {
        return new PolymerMapCodec<T>(codec, AllOfEnchantmentEffects.ValueEffects.CODEC, new AllOfEnchantmentEffects.ValueEffects(List.of()));
    }

    public static <T extends EnchantmentLocationBasedEffect> MapCodec<T> ofEnchantmentLocationBasedEffect(MapCodec<T> codec) {
        return new PolymerMapCodec<T>(codec, AllOfEnchantmentEffects.LocationBasedEffects.CODEC, new AllOfEnchantmentEffects.LocationBasedEffects(List.of()));
    }

    public static <T extends EnchantmentEntityEffect> MapCodec<T> ofEnchantmentEntityEffect(MapCodec<T> codec) {
        return new PolymerMapCodec<T>(codec, AllOfEnchantmentEffects.EntityEffects.CODEC, new AllOfEnchantmentEffects.EntityEffects(List.of()));
    }

    public static <T extends EnchantmentLevelBasedValue> MapCodec<T> ofEnchantmentLevelBasedValue(MapCodec<T> codec) {
        return new PolymerMapCodec<T>(codec, EnchantmentLevelBasedValue.Constant.TYPE_CODEC, new EnchantmentLevelBasedValue.Constant(0));
    }

    public Object fallbackValue() {
        return fallbackValue;
    }

    public MapCodec<Object> fallbackCodec() {
        return fallbackCodec;
    }

    @Override
    public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
        return this.selfCodec.keys(ops);
    }

    @Override
    public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
        return this.selfCodec.decode(ops, input);
    }

    @Override
    public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
        if (PolymerCommonUtils.isServerNetworkingThreadWithContext()) {
            return this.fallbackCodec.encode(this.fallbackValue, ops, prefix);
        }

        return this.selfCodec.encode(input, ops, prefix);
    }
}
