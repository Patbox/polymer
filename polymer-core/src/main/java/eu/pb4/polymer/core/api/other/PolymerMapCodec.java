package eu.pb4.polymer.core.api.other;

import com.mojang.serialization.*;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.Registry;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PolymerMapCodec<T> extends MapCodec<T> implements PolymerObject {
    private static final Map<MapCodec<?>, PolymerMapCodec<?>> OVERLAYS = new IdentityHashMap<>();
    private final MapCodec<T> selfCodec;
    private final Transform<T, Object> fallbackValue;

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
        return ofStatic(codec, new AllOf.ValueEffects(List.of()));
    }

    public static <T extends EnchantmentLocationBasedEffect> MapCodec<T> ofEnchantmentLocationBasedEffect(MapCodec<T> codec) {
        return ofStatic(codec, new AllOf.LocationBasedEffects(List.of()));
    }

    public static <T extends EnchantmentEntityEffect> MapCodec<T> ofEnchantmentEntityEffect(MapCodec<T> codec) {
        return ofStatic(codec, new AllOf.EntityEffects(List.of()));
    }

    public static <T extends LevelBasedValue> MapCodec<T> ofEnchantmentLevelBasedValue(MapCodec<T> codec) {
        return ofStatic(codec, new LevelBasedValue.Constant(0));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> PolymerMapCodec<T> getOverlay(MapEncoder<T> codec) {
        return codec instanceof PolymerMapCodec<T> ? (PolymerMapCodec<T>) codec : (PolymerMapCodec<T>) OVERLAYS.get(codec);
    }

    public static <T extends A, A> void setOverlay(Registry<MapCodec<A>> registry, MapCodec<T> sourceCodec, PolymerMapCodec<T> codec) {
        //noinspection unchecked
        RegistrySyncUtils.setServerEntry(registry, (MapCodec<A>) sourceCodec);
        OVERLAYS.put(sourceCodec, codec);
    }

    @ApiStatus.Internal
    public Object getPolymerReplacement(T data, PacketContext context) {
        return this.fallbackValue.transform(data, context);
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
