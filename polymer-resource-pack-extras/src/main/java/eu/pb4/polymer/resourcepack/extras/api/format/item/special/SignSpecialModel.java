package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Optional;

public record SignSpecialModel(WoodType woodType, PlainSignBlock.Attachment attachment,
                               Optional<Identifier> texture) implements SpecialModel {
    public static final MapCodec<SignSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    WoodType.CODEC.fieldOf("wood_type").forGetter(SignSpecialModel::woodType),
                    PlainSignBlock.Attachment.CODEC.optionalFieldOf("attachment", PlainSignBlock.Attachment.GROUND).forGetter(SignSpecialModel::attachment),
                    Identifier.CODEC.optionalFieldOf("texture").forGetter(SignSpecialModel::texture)
            ).apply(instance, SignSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
