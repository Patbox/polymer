package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record ChestSpecialModel(Identifier texture, float openness) implements SpecialModel {
    public static final MapCodec<ChestSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(ChestSpecialModel::texture),
                    Codec.FLOAT.optionalFieldOf("openness", 0f).forGetter(ChestSpecialModel::openness)
            ).apply(instance, ChestSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
