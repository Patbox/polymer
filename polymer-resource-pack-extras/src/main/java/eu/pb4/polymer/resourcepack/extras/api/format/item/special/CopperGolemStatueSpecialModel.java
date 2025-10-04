package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.util.Identifier;

public record CopperGolemStatueSpecialModel(Identifier texture, CopperGolemStatueBlock.Pose pose) implements SpecialModel {
    public static final MapCodec<CopperGolemStatueSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(CopperGolemStatueSpecialModel::texture),
                    CopperGolemStatueBlock.Pose.CODEC.fieldOf("pose").forGetter(CopperGolemStatueSpecialModel::pose)
            ).apply(instance, CopperGolemStatueSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
