package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public record BannerSpecialModel(DyeColor color) implements SpecialModel {
    public static final MapCodec<BannerSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DyeColor.CODEC.fieldOf("color").forGetter(BannerSpecialModel::color)
            ).apply(instance, BannerSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
