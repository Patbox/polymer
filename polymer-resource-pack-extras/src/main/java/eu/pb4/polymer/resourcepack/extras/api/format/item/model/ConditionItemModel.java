package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.bool.BooleanProperty;

import java.util.Optional;


public record ConditionItemModel(BooleanProperty property, ItemModel onTrue, ItemModel onFalse, Optional<Transformation> transformation) implements ItemModel {
    public static final MapCodec<ConditionItemModel> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BooleanProperty.CODEC.forGetter(ConditionItemModel::property),
                    ItemModel.CODEC.fieldOf("on_true").forGetter(ConditionItemModel::onTrue),
                    ItemModel.CODEC.fieldOf("on_false").forGetter(ConditionItemModel::onFalse),
                    Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(ConditionItemModel::transformation)
            ).apply(instance, ConditionItemModel::new)
    );

    public ConditionItemModel(BooleanProperty property, ItemModel onTrue, ItemModel onFalse) {
        this(property, onTrue, onFalse, Optional.empty());
    }

    @Override
    public ItemModel replaceChildren(Replacer replacer) {
        var onTrue = replacer.modifyDeep(this, onTrue());
        var onFalse = replacer.modifyDeep(this, onFalse());
        return new ConditionItemModel(property,
                onTrue != null ? onTrue : EmptyItemModel.INSTANCE,
                onFalse != null ? onFalse : EmptyItemModel.INSTANCE,
                this.transformation
        );
    }

    @Override
    public MapCodec<ConditionItemModel> codec() {
        return CODEC;
    }
}