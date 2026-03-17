package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jspecify.annotations.Nullable;

public record ChestSpecialModel(Identifier texture, ChestType chestType, float openness) implements SpecialModel {
    public static final MapCodec<ChestSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(ChestSpecialModel::texture),
                    ChestType.CODEC.optionalFieldOf("chest_type", ChestType.SINGLE).forGetter(ChestSpecialModel::chestType),
                    Codec.FLOAT.optionalFieldOf("openness", 0f).forGetter(ChestSpecialModel::openness)
            ).apply(instance, ChestSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
