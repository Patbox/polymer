package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.special.SpecialModel;
import net.minecraft.resources.Identifier;

public record SpecialItemModel(Identifier base, SpecialModel specialModel) implements ItemModel {
        public static final MapCodec<SpecialItemModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(SpecialItemModel::base),
                SpecialModel.CODEC.fieldOf("model").forGetter(SpecialItemModel::specialModel)
        ).apply(instance, SpecialItemModel::new));

    @Override
    public MapCodec<? extends ItemModel> codec() {
        return CODEC;
    }
}