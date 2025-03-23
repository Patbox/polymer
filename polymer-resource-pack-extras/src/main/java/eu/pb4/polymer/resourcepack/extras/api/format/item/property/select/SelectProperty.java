package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.SelectItemModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.stream.Collectors;

public interface SelectProperty<T> {
    Codecs.IdMapper<Identifier, Type<?, ?>> TYPES = Util.make(new Codecs.IdMapper<>(), m -> {
        m.put(Identifier.ofVanilla("custom_model_data"), CustomModelDataStringProperty.TYPE);
        m.put(Identifier.ofVanilla("main_hand"), MainHandProperty.TYPE);
        m.put(Identifier.ofVanilla("charge_type"), ChargeTypeProperty.TYPE);
        m.put(Identifier.ofVanilla("trim_material"), TrimMaterialProperty.TYPE);
        m.put(Identifier.ofVanilla("block_state"), ItemBlockStateProperty.TYPE);
        m.put(Identifier.ofVanilla("display_context"), DisplayContextProperty.TYPE);
        m.put(Identifier.ofVanilla("local_time"), LocalTimeProperty.TYPE);
        m.put(Identifier.ofVanilla("context_entity_type"), ContextEntityTypeProperty.TYPE);
        m.put(Identifier.ofVanilla("context_dimension"), ContextDimensionProperty.TYPE);
        m.put(Identifier.ofVanilla("component"), ComponentSelectProperty.getTypeInstance());
    });
    Codec<Type<?, ?>> CODEC = TYPES.getCodec(Identifier.CODEC);

    Type<? extends SelectProperty<T>, T> type();

    record Type<T extends SelectProperty<Y>, Y>(MapCodec<SelectItemModel.Switch<T, Y>> switchCodec) {
        public Type(MapCodec<T> mainCodec, Codec<Y> valueCodec) {
            this(codec(mainCodec, valueCodec));
        }

        private static <Y, T extends SelectProperty<Y>> MapCodec<SelectItemModel.Switch<T, Y>> codec(MapCodec<T> mainCodec, Codec<Y> valueCodec) {
            var codec = SelectItemModel.Case.createCodec(valueCodec)
                    .listOf()
                    .validate(cases -> {
                                if (cases.isEmpty()) {
                                    return DataResult.error(() -> "Empty case list");
                                } else {
                                    Multiset<Y> multiset = HashMultiset.create();

                                    for (var switchCase : cases) {
                                        multiset.addAll(switchCase.values());
                                    }

                                    return multiset.size() != multiset.entrySet().size()
                                            ? DataResult.error(
                                            () -> "Duplicate case conditions: "
                                                    + multiset.entrySet()
                                                    .stream()
                                                    .filter(entry -> entry.getCount() > 1)
                                                    .map(entry -> entry.getElement().toString())
                                                    .collect(Collectors.joining(", "))
                                    ) : DataResult.success(cases);
                                }
                            }
                    );
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            mainCodec.forGetter(SelectItemModel.Switch::property), codec.fieldOf("cases").forGetter(SelectItemModel.Switch::cases)
                    ).apply(instance, SelectItemModel.Switch::new)
            );
        }

        public static <T> MapCodec<List<SelectItemModel.Case<T>>> createCaseListCodec(Codec<T> conditionCodec) {
            return SelectItemModel.Case.createCodec(conditionCodec).listOf().fieldOf("cases");
        }
    }

}
