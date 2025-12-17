package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

public record ConduitSpecialModel() implements SpecialModel {
    public static final MapCodec<ConduitSpecialModel> CODEC = MapCodec.unit(new ConduitSpecialModel());

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
