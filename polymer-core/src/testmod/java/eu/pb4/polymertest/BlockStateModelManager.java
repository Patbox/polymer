package eu.pb4.polymertest;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.BlockStateAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateModelVariant;
import eu.pb4.polymer.resourcepack.extras.api.format.blockstate.StateMultiPartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BlockStateModelManager {
    public static final Map<String, Map<String, List<StateModelVariant>>> UV_LOCKED_MODELS = new HashMap<>();
    public static final Map<BlockState, List<ModelGetter>> MAP = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockStateModelManager.class);

    public static void addBlock(Identifier identifier, Block block, BlockStateAsset modelDef) {
        try {
            var rand = RandomSource.create(123);

            if (modelDef.variants().isPresent()) {
                var list = new ArrayList<Pair<BlockStatePredicate, List<ModelData>>>();
                parseVariants(block, modelDef.variants().get(), list);

                for (var pair : list) {
                    for (var state : block.getStateDefinition().getPossibleStates()) {
                        if (pair.getFirst().test(state)) {
                            MAP.put(state, List.of(ModelGetter.of(pair.getSecond())));
                        }
                    }
                }
            }

            if (modelDef.multipart().isPresent()) {
                var list = new ArrayList<Pair<Predicate<BlockState>, List<ModelData>>>();
                parseMultipart(block, modelDef.multipart().get(), list);

                for (var pair : list) {
                    for (var state : block.getStateDefinition().getPossibleStates()) {
                        if (pair.getFirst().test(state)) {
                            var objects = new ArrayList<ModelGetter>();
                            if (MAP.containsKey(state)) {
                                objects.addAll(MAP.get(state));
                            }
                            objects.add(ModelGetter.of(pair.getSecond()));
                            MAP.put(state, objects);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to decode model for {}", identifier, e);
        }
    }

    private static void parseMultipart(Block block, List<StateMultiPartDefinition> multiPartDefinition, ArrayList<Pair<Predicate<BlockState>, List<ModelData>>> list) {
        for (var part : multiPartDefinition) {
            Predicate<BlockState> preds;

            if (part.when().isEmpty()) {
                preds = BlockStatePredicate.forBlock(block);
            } else {
                var when = part.when().get();
                preds = parsePredicate(block, when);
            }

            var modelData = parseBaseVariants(part.apply());
            list.add(new Pair<>(preds, modelData));
        }
    }

    @SuppressWarnings({"ReassignedVariable", "unchecked", "rawtypes", "StatementWithEmptyBody"})
    private static Predicate<BlockState> parsePredicate(Block block, StateMultiPartDefinition.Condition when) {
        if (when instanceof StateMultiPartDefinition.KeyValueCondition(
                Map<String, StateMultiPartDefinition.KeyValueCondition.Terms> tests
        )) {
            Predicate<BlockState> predicate = x -> true;
            for (var pair : tests.entrySet()) {
                var prop = (Property) block.getStateDefinition().getProperty(pair.getKey());
                if (prop == null) {
                    continue;
                }
                predicate = predicate.and(parseTerms(pair.getValue().entries(), block, prop));
            }
            return predicate;
        } else if (when instanceof StateMultiPartDefinition.CombinedCondition(
                StateMultiPartDefinition.CombinedCondition.Operation operation,
                List<StateMultiPartDefinition.Condition> terms
        )) {
            var predicates = terms.stream().map(x -> parsePredicate(block, x)).toArray(Predicate[]::new);
            return switch (operation) {
                case OR -> Util.anyOf(predicates);
                case AND -> Util.allOf(predicates);
            };
        }

        return x -> false;
    }

    private static <O, S extends StateHolder<O, S>, T extends Comparable<T>> Predicate<S> parseTerms(List<StateMultiPartDefinition.KeyValueCondition.Term> entries, final O owner, final Property<T> property) {
        Predicate<T> allowedValueTest = Util.anyOf(Lists.transform(entries, t -> parseTerm(owner, property, t)));
        List<T> allowedValues = new ArrayList<>(property.getPossibleValues());
        int allValuesCount = allowedValues.size();
        allowedValues.removeIf(allowedValueTest.negate());
        int allowedValuesCount = allowedValues.size();
        if (allowedValuesCount == 0) {
            return blockState -> false;
        } else {
            int rejectedValuesCount = allValuesCount - allowedValuesCount;
            if (rejectedValuesCount == 0) {
                return blockState -> true;
            } else {
                boolean negate;
                List<T> valuesToMatch;
                if (allowedValuesCount <= rejectedValuesCount) {
                    negate = false;
                    valuesToMatch = allowedValues;
                } else {
                    negate = true;
                    List<T> rejectedValues = new ArrayList<>(property.getPossibleValues());
                    rejectedValues.removeIf(allowedValueTest);
                    valuesToMatch = rejectedValues;
                }

                if (valuesToMatch.size() == 1) {
                    T expectedValue = valuesToMatch.getFirst();
                    return state -> {
                        T value = state.getValue(property);
                        return expectedValue.equals(value) ^ negate;
                    };
                } else {
                    return state -> {
                        T value = state.getValue(property);
                        return valuesToMatch.contains(value) ^ negate;
                    };
                }
            }
        }
    }

    private static <T extends Comparable<T>> T getValueOrThrow(final Object owner, final Property<T> property, final String input) {
        Optional<T> value = property.getValue(input);
        if (value.isEmpty()) {
            throw new RuntimeException(
                    String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s'", input, property, owner)
            );
        } else {
            return value.get();
        }
    }

    private static <T extends Comparable<T>> Predicate<T> parseTerm(final Object owner, final Property<T> property, final StateMultiPartDefinition.KeyValueCondition.Term term) {
        T parsedValue = getValueOrThrow(owner, property, term.value());
        return term.negated() ? value -> !value.equals(parsedValue) : value -> value.equals(parsedValue);
    }

    private static void parseVariants(Block block, Map<String, List<StateModelVariant>> modelDef, ArrayList<Pair<BlockStatePredicate, List<ModelData>>> list) {
        parseVariants(block, modelDef, (a, b) -> {
            var modelData = parseBaseVariants(b);
            list.add(new Pair<>(a, modelData));
        });
    }

    public static void parseVariants(Block block, Map<String, List<StateModelVariant>> modelDef, BiConsumer<BlockStatePredicate, List<StateModelVariant>> consumer) {
        start:
        for (var pair : modelDef.entrySet()) {
            var stateMap = pair.getKey().split(",");

            var predicate = BlockStatePredicate.forBlock(block);


            for (var statePair : stateMap) {
                if (!statePair.isEmpty()) {
                    var split = statePair.split("=", 2);
                    var prop = (Property) block.getStateDefinition().getProperty(split[0]);

                    if (prop == null) {
                        continue start;
                    }

                    predicate.where(prop, x -> prop.getName((Comparable) x).equals(split[1]));
                }
            }

            consumer.accept(predicate, pair.getValue());
        }
    }

    private static List<ModelData> parseBaseVariants(List<StateModelVariant> value) {
        var modelData = new ArrayList<ModelData>();

        for (var v : value) {
            modelData.add(new ModelData(v.model(), new Quaternionf()
                    .rotateY(-Mth.DEG_TO_RAD * v.y())
                    .rotateX(Mth.DEG_TO_RAD * v.x()),
                    v.weigth(), v.uvlock()
            ));
        }

        return modelData;
    }

    public interface ModelGetter {
        static ModelGetter of(List<ModelData> data) {
            if (data.size() == 1) {
                return new SingleGetter(data.get(0));
            }

            return WeightedGetter.create(data);
        }

        ModelData getModel(RandomSource random);
    }


    private record SingleGetter(ModelData data) implements ModelGetter {
        @Override
        public ModelData getModel(RandomSource random) {
            return this.data;
        }
    }

    private record WeightedGetter(WeightedList<ModelData> data, int weightedSum) implements ModelGetter {
        public static ModelGetter create(List<ModelData> data) {
            var list = new ArrayList<Weighted<ModelData>>();
            for (var d : data) {
                list.add(new Weighted<>(d, d.weight));
            }
            var x = WeightedRandom.getTotalWeight(list, Weighted::weight);

            return new WeightedGetter(WeightedList.of(list), x);
        }

        @Override
        public ModelData getModel(RandomSource random) {
            return this.data.getRandomOrThrow(random);
        }
    }

    public record ModelData(Identifier identifier, Quaternionfc quaternionfc, int weight, boolean uvlock) {
    }
}
