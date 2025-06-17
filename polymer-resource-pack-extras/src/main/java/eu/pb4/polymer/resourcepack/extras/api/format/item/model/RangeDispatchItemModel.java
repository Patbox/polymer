package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric.NumericProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record RangeDispatchItemModel(NumericProperty property, float scale, List<Entry> entries, Optional<ItemModel> fallback) implements ItemModel {
    public static final MapCodec<RangeDispatchItemModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumericProperty.CODEC.forGetter(RangeDispatchItemModel::property),
            Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(RangeDispatchItemModel::scale),
            Entry.CODEC.listOf().fieldOf("entries").forGetter(RangeDispatchItemModel::entries),
            ItemModel.CODEC.optionalFieldOf("fallback").forGetter(RangeDispatchItemModel::fallback)
    ).apply(instance, RangeDispatchItemModel::new));

    @Override
    public MapCodec<? extends ItemModel> codec() {
        return CODEC;
    }

    public record Entry(float threshold, ItemModel model) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("threshold").forGetter(Entry::threshold),
                ItemModel.CODEC.fieldOf("model").forGetter(Entry::model)
        ).apply(instance, Entry::new));
    }

    public static Builder builder(NumericProperty property) {
        return new Builder(property);
    }

    @Override
    public ItemModel replaceChildren(Replacer replacer) {
        var list = new ArrayList<Entry>(this.entries.size());
        for (var entry : this.entries) {
            var model = replacer.modifyDeep(this, entry.model);
            if (entry.model != model) {
                list.add(new Entry(entry.threshold, model));
            } else {
                list.add(entry);
            }
        }

        return new RangeDispatchItemModel(this.property, scale, list, fallback.map(model -> replacer.modifyDeep(this, model)));
    }

    public static class Builder {
        private final NumericProperty property;
        private final List<Entry> entries = new ArrayList<>();
        private float scale = 1;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<ItemModel> fallbackModel = Optional.empty();
        private Builder(NumericProperty property) {
            this.property = property;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder entry(float threshold, ItemModel model) {
            this.entries.add(new Entry(threshold, model));
            return this;
        }

        public Builder fallback(ItemModel model) {
            this.fallbackModel = Optional.ofNullable(model);
            return this;
        }

        public RangeDispatchItemModel build() {
            return new RangeDispatchItemModel(this.property, this.scale, new ArrayList<>(this.entries), this.fallbackModel);
        }
    }
}
