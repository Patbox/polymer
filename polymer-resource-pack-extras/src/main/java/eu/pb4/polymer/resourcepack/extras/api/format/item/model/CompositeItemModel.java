package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CompositeItemModel(List<ItemModel> models, Optional<Transformation> transformation) implements ItemModel {
    public static final MapCodec<CompositeItemModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemModel.CODEC.listOf().fieldOf("models").forGetter(CompositeItemModel::models),
            Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(CompositeItemModel::transformation)
    ).apply(instance, CompositeItemModel::new));


    public CompositeItemModel(List<ItemModel> models) {
        this(models, Optional.empty());
    }

    @Override
    public MapCodec<? extends ItemModel> codec() {
        return CODEC;
    }

    @Override
    public ItemModel replaceChildren(Replacer replacer) {
        var list = new ArrayList<ItemModel>();
        for (var model : models) {
            model = replacer.modifyDeep(this, model);
            if (model != null) {
                list.add(model);
            }
        }
        return new CompositeItemModel(list);
    }
}
