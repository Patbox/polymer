package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.special.SpecialModel;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record SpecialItemModel(Identifier base, SpecialModel specialModel, Optional<Transformation> transformation) implements ItemModel {
        public static final MapCodec<SpecialItemModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(SpecialItemModel::base),
                SpecialModel.CODEC.fieldOf("model").forGetter(SpecialItemModel::specialModel),
                Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(SpecialItemModel::transformation)
        ).apply(instance, SpecialItemModel::new));

        public SpecialItemModel(Identifier base, SpecialModel specialModel) {
            this(base, specialModel, Optional.empty());
        }

    @Override
    public MapCodec<? extends ItemModel> codec() {
        return CODEC;
    }
}