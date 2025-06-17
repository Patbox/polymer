package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.property.select.SelectProperty;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SelectItemModel<T extends SelectProperty<V>, V>(Switch<T, V> switchValue,
                                                              Optional<ItemModel> fallback) implements ItemModel {
    public static final MapCodec<SelectItemModel<?, ?>> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Switch.CODEC.forGetter(SelectItemModel::switchValue),
                    ItemModel.CODEC.optionalFieldOf("fallback").forGetter(SelectItemModel::fallback)
            ).apply(instance, SelectItemModel::new)
    );

    @Override
    public MapCodec<SelectItemModel<?, ?>> codec() {
        return CODEC;
    }

    public record Switch<T extends SelectProperty<V>, V>(T property, List<Case<V>> cases) {
        public static final MapCodec<Switch<?, ?>> CODEC = SelectProperty.CODEC
                .dispatchMap("property", unbakedSwitch -> unbakedSwitch.property().type(), SelectProperty.Type::switchCodec);
    }

    public record Case<T>(List<T> values, ItemModel model) {
        public static <T> Codec<Case<T>> createCodec(Codec<T> valueCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                            Codecs.nonEmptyList(Codecs.listOrSingle(valueCodec)).fieldOf("when").forGetter(Case::values),
                            ItemModel.CODEC.fieldOf("model").forGetter(Case::model)
                    ).apply(instance, Case::new)
            );
        }
    }

    @Override
    public ItemModel replaceChildren(Replacer replacer) {
        var list = new ArrayList<Case<V>>(this.switchValue.cases.size());
        for (var entry : this.switchValue.cases) {
            var model = replacer.modifyDeep(this, entry.model);
            if (entry.model != model) {
                list.add(new Case<>(entry.values, model));
            } else {
                list.add(entry);
            }
        }

        return new SelectItemModel<>(new Switch<>(this.switchValue.property, list), fallback.map(model -> replacer.modifyDeep(this, model)));
    }

    public static <T extends SelectProperty<V>, V> Builder<T, V> builder(T property) {
        return new Builder<>(property);
    }

    public static class Builder<T extends SelectProperty<V>, V> {
        private final T property;
        private final List<Case<V>> cases = new ArrayList<>();
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<ItemModel> fallbackModel = Optional.empty();
        private Builder(T property) {
            this.property = property;
        }

        public Builder<T, V> withCase(V value, ItemModel model) {
            this.cases.add(new Case<>(List.of(value), model));
            return this;
        }

        public Builder<T, V> withCase(List<V> value, ItemModel model) {
            this.cases.add(new Case<>(value, model));
            return this;
        }

        public Builder<T, V> fallback(ItemModel model) {
            this.fallbackModel = Optional.ofNullable(model);
            return this;
        }

        public SelectItemModel<T, V> build() {
            return new SelectItemModel<>(new Switch<>(this.property, new ArrayList<>(this.cases)), this.fallbackModel);
        }
    }
}
