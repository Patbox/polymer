package eu.pb4.polymer.resourcepack.extras.api.format.blockstate;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public record StateMultiPartDefinition(Optional<Condition> when, List<StateModelVariant> apply) {
    public static final Codec<StateMultiPartDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Condition.CODEC.optionalFieldOf("when").forGetter(StateMultiPartDefinition::when),
                    StateModelVariant.CODEC.fieldOf("apply").forGetter(StateMultiPartDefinition::apply)
            ).apply(instance, StateMultiPartDefinition::new)
    );


    public sealed interface Condition permits CombinedCondition, KeyValueCondition {
        Codec<Condition> CODEC = Codec.recursive(
                "condition",
                self -> {
                    Codec<CombinedCondition> combinerCodec = Codec.simpleMap(CombinedCondition.Operation.CODEC, self.listOf(), StringRepresentable.keys(CombinedCondition.Operation.values()))
                            .codec()
                            .comapFlatMap(map -> {
                                if (map.size() != 1) {
                                    return DataResult.error(() -> "Invalid map size for combiner condition, expected exactly one element");
                                } else {
                                    var entry = map.entrySet().iterator().next();
                                    return DataResult.success(new CombinedCondition(entry.getKey(), entry.getValue()));
                                }
                            }, condition -> Map.of(condition.operation(), condition.terms()));
                    return Codec.either(combinerCodec, KeyValueCondition.CODEC).flatComapMap(either -> either.map(l -> l, r -> (Condition) r), condition -> {
                        return switch (condition) {
                            case CombinedCondition combiner ->
                                    DataResult.<Either<CombinedCondition, KeyValueCondition>>success(Either.left(combiner));
                            case KeyValueCondition keyValue ->
                                    DataResult.<Either<CombinedCondition, KeyValueCondition>>success(Either.right(keyValue));
                            default ->
                                    DataResult.<Either<CombinedCondition, KeyValueCondition>>error(() -> "Unrecognized condition");
                        };
                    });
                }
        );
    }

    public record CombinedCondition(CombinedCondition.Operation operation, List<Condition> terms) implements Condition {
        public enum Operation implements StringRepresentable {
            AND("AND"),
            OR("OR");

            public static final Codec<CombinedCondition.Operation> CODEC = StringRepresentable.fromEnum(CombinedCondition.Operation::values);
            private final String name;

            Operation(final String name) {
                this.name = name;
            }

            @Override
            public String getSerializedName() {
                return this.name;
            }
        }
    }

    public record KeyValueCondition(Map<String, KeyValueCondition.Terms> tests) implements Condition {
        public static final Codec<KeyValueCondition> CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap(Codec.STRING, KeyValueCondition.Terms.CODEC))
                .xmap(KeyValueCondition::new, KeyValueCondition::tests);

        public record Term(String value, boolean negated) {
            private static final String NEGATE = "!";

            public Term(String value, boolean negated) {
                if (value.isEmpty()) {
                    throw new IllegalArgumentException("Empty term");
                } else {
                    this.value = value;
                    this.negated = negated;
                }
            }

            public static KeyValueCondition.Term parse(final String value) {
                return value.startsWith("!") ? new KeyValueCondition.Term(value.substring(1), true) : new KeyValueCondition.Term(value, false);
            }

            public String toString() {
                return this.negated ? "!" + this.value : this.value;
            }
        }

        public record Terms(List<KeyValueCondition.Term> entries) {
            private static final char SEPARATOR = '|';
            private static final Joiner JOINER = Joiner.on('|');
            private static final Splitter SPLITTER = Splitter.on('|');
            private static final Codec<String> LEGACY_REPRESENTATION_CODEC = Codec.either(Codec.INT, Codec.BOOL)
                    .flatComapMap(either -> either.map(String::valueOf, String::valueOf), o -> DataResult.error(() -> "This codec can't be used for encoding"));
            public static final Codec<KeyValueCondition.Terms> CODEC = Codec.withAlternative(Codec.STRING, LEGACY_REPRESENTATION_CODEC)
                    .comapFlatMap(KeyValueCondition.Terms::parse, KeyValueCondition.Terms::toString);

            public Terms(List<KeyValueCondition.Term> entries) {
                if (entries.isEmpty()) {
                    throw new IllegalArgumentException("Empty value for property");
                } else {
                    this.entries = entries;
                }
            }

            public static DataResult<KeyValueCondition.Terms> parse(final String value) {
                List<KeyValueCondition.Term> terms = SPLITTER.splitToStream(value).map(KeyValueCondition.Term::parse).toList();
                if (terms.isEmpty()) {
                    return DataResult.error(() -> "Empty value for property");
                } else {
                    for (KeyValueCondition.Term entry : terms) {
                        if (entry.value.isEmpty()) {
                            return DataResult.error(() -> "Empty term in value '" + value + "'");
                        }
                    }

                    return DataResult.success(new KeyValueCondition.Terms(terms));
                }
            }

            public String toString() {
                return JOINER.join(this.entries);
            }
        }
    }
}
