package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.ItemTintSource;
import java.util.List;
import java.util.Optional;

import net.minecraft.resources.Identifier;

public record BasicItemModel(Identifier model, Optional<Transformation> transformation, List<ItemTintSource> tints) implements ItemModel {
    public static final MapCodec<BasicItemModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(BasicItemModel::model),
            Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(BasicItemModel::transformation),
            ItemTintSource.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(BasicItemModel::tints)
    ).apply(instance, BasicItemModel::new));

    public BasicItemModel(Identifier model) {
        this(model, List.of());
    }
    public BasicItemModel(Identifier model, List<ItemTintSource> tints) {
        this(model, Optional.empty(), tints);
    }

    @Override
    public MapCodec<? extends ItemModel> codec() {
        return CODEC;
    }
}
