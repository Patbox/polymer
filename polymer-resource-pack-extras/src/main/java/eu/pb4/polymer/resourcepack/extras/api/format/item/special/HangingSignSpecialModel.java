package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Optional;

public record HangingSignSpecialModel(WoodType woodType, HangingSignBlock.Attachment attachment,
                                      Optional<Identifier> texture) implements SpecialModel {
    public static final MapCodec<HangingSignSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    WoodType.CODEC.fieldOf("wood_type").forGetter(HangingSignSpecialModel::woodType),
                    HangingSignBlock.Attachment.CODEC.optionalFieldOf("attachment", HangingSignBlock.Attachment.CEILING_MIDDLE).forGetter(HangingSignSpecialModel::attachment),
                    Identifier.CODEC.optionalFieldOf("texture").forGetter(HangingSignSpecialModel::texture)
            ).apply(instance, HangingSignSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
