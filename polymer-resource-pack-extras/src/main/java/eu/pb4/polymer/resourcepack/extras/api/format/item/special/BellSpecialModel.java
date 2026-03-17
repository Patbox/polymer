package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;

public record BellSpecialModel() implements SpecialModel {
    public static final MapCodec<BellSpecialModel> CODEC = MapCodec.unit(new BellSpecialModel());

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
