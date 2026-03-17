package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;

public record BannerSpecialModel(DyeColor color, BannerBlock.AttachmentType attachment) implements SpecialModel {
    public static final MapCodec<BannerSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DyeColor.CODEC.fieldOf("color").forGetter(BannerSpecialModel::color),
                    BannerBlock.AttachmentType.CODEC.optionalFieldOf("attachment", BannerBlock.AttachmentType.GROUND).forGetter(BannerSpecialModel::attachment)
            ).apply(instance, BannerSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
