package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.WoodType;

public record HangingSignSpecialModel(WoodType woodType, Optional<Identifier> texture) implements SpecialModel {
    public static final MapCodec<HangingSignSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    WoodType.CODEC.fieldOf("wood_type").forGetter(HangingSignSpecialModel::woodType),
                    Identifier.CODEC.optionalFieldOf("texture").forGetter(HangingSignSpecialModel::texture)
            ).apply(instance, HangingSignSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
