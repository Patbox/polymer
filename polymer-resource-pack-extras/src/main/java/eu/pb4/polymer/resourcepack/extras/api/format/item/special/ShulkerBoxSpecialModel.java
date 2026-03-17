package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public record ShulkerBoxSpecialModel(Identifier texture, float openness) implements SpecialModel {
    public static final MapCodec<ShulkerBoxSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(ShulkerBoxSpecialModel::texture),
                    Codec.FLOAT.optionalFieldOf("openness", 0f).forGetter(ShulkerBoxSpecialModel::openness)
            ).apply(instance, ShulkerBoxSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
