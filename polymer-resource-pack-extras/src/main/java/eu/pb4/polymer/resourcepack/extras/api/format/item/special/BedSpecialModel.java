package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BedPart;

public record BedSpecialModel(Identifier texture, BedPart part) implements SpecialModel {
    public static final MapCodec<BedSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("texture").forGetter(BedSpecialModel::texture),
                    BedPart.CODEC.fieldOf("part").forGetter(BedSpecialModel::part)
            ).apply(instance, BedSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
