package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;

public record HeadSpecialModel(SkullBlock.Type kind, Optional<Identifier> textureOverride,
                               float animation) implements SpecialModel {
    public static final MapCodec<HeadSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SkullBlock.Type.CODEC.fieldOf("kind").forGetter(HeadSpecialModel::kind),
                    Identifier.CODEC.optionalFieldOf("texture").forGetter(HeadSpecialModel::textureOverride),
                    Codec.FLOAT.optionalFieldOf("animation", 0f).forGetter(HeadSpecialModel::animation)
            ).apply(instance, HeadSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
