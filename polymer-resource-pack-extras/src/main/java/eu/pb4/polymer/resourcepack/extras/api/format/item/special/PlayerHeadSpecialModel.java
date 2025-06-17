package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;

public record PlayerHeadSpecialModel() implements SpecialModel {
    public static final MapCodec<PlayerHeadSpecialModel> CODEC = MapCodec.unit(new PlayerHeadSpecialModel());

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
