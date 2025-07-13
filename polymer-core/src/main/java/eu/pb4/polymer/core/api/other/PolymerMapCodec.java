package eu.pb4.polymer.core.api.other;

import com.mojang.serialization.*;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.minecraft.dialog.body.DialogBody;
import net.minecraft.dialog.input.InputControl;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.AllOfEnchantmentEffects;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.stream.Stream;

public class PolymerMapCodec<T> extends MapCodec<T> implements PolymerObject {
    private final MapCodec<T> selfCodec;
    private final Transform<T, Object> fallbackValue;

    @Deprecated(forRemoval = true)
    public <K> PolymerMapCodec(MapCodec<T> selfCodec, MapCodec<K> fallbackCodec, K fallbackValue) {
        this(selfCodec,  (x, ctx) -> fallbackValue);
    }

    private PolymerMapCodec(MapCodec<T> selfCodec, Transform<T, Object> fallbackValue) {
        this.selfCodec = selfCodec;
        this.fallbackValue = fallbackValue;
    }

    public static <T extends K, K> MapCodec<T> ofStatic(MapCodec<T> selfCodec, K fallbackValue) {
        return new PolymerMapCodec<T>(selfCodec, (x, ctx) -> fallbackValue);
    }

    public static <T extends K, K> MapCodec<T> ofDynamic(MapCodec<T> codec, Transform<T, K> transform) {
        //noinspection unchecked
        return new PolymerMapCodec<T>(codec, (Transform<T, Object>) transform);
    }

    public static <T extends Dialog> MapCodec<T> ofDialog(MapCodec<T> codec, Transform<T, Dialog> transform) {
        return ofDynamic(codec,  transform);
    }

    public static <T extends DialogBody> MapCodec<T> ofDialogBody(MapCodec<T> codec, Transform<T, DialogBody> transform) {
        return ofDynamic(codec, transform);
    }

    public static <T extends InputControl> MapCodec<T> ofDialogInputControl(MapCodec<T> codec, Transform<T, InputControl> transform) {
        return ofDynamic(codec,  transform);
    }

    public static <T extends EnchantmentValueEffect> MapCodec<T> ofEnchantmentValueEffect(MapCodec<T> codec) {
        return ofStatic(codec, new AllOfEnchantmentEffects.ValueEffects(List.of()));
    }

    public static <T extends EnchantmentLocationBasedEffect> MapCodec<T> ofEnchantmentLocationBasedEffect(MapCodec<T> codec) {
        return ofStatic(codec, new AllOfEnchantmentEffects.LocationBasedEffects(List.of()));
    }

    public static <T extends EnchantmentEntityEffect> MapCodec<T> ofEnchantmentEntityEffect(MapCodec<T> codec) {
        return ofStatic(codec, new AllOfEnchantmentEffects.EntityEffects(List.of()));
    }

    public static <T extends EnchantmentLevelBasedValue> MapCodec<T> ofEnchantmentLevelBasedValue(MapCodec<T> codec) {
        return ofStatic(codec, new EnchantmentLevelBasedValue.Constant(0));
    }

    @ApiStatus.Internal
    public Object getPolymerReplacement(T data, PacketContext context) {
        return this.fallbackValue.transform(data, context);
    }


    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public Object fallbackValue() {
        return fallbackValue;
    }

    @ApiStatus.Internal
    @Deprecated(forRemoval = true)
    public MapCodec<Object> fallbackCodec() {
        return MapCodec.unit(Unit.INSTANCE);
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
        return this.selfCodec.encode(input, ops, prefix);
    }

    public interface Transform<T extends K, K> {
        K transform(T data, PacketContext context);
    }
}
