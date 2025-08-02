package eu.pb4.polymer.resourcepack.extras.api.format.blockstate;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record StateMultiPartDefinition(When when, List<StateModelVariant> apply) {
    public static final Codec<StateMultiPartDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    When.CODEC.optionalFieldOf("when", When.DEFAULT).forGetter(StateMultiPartDefinition::when),
                    StateModelVariant.CODEC.fieldOf("apply").forGetter(StateMultiPartDefinition::apply)
            ).apply(instance, StateMultiPartDefinition::new)
    );

    public record When(Optional<List<Map<String, String>>> or, Optional<List<Map<String, String>>> and,
                       Optional<Map<String, String>> base) {
        public static final When DEFAULT = new When(Optional.empty(), Optional.empty(), Optional.empty());

        private static final Codec<Map<String, String>> STR_MAP = SortedMapCodec.of(Codec.STRING, Codec.withAlternative(Codec.STRING, Codecs.BASIC_OBJECT, String::valueOf));
        private static final Codec<List<Map<String, String>>> LIST_STR_MAP = STR_MAP.listOf();
        public static final Codec<When> CODEC = Codec.either(
                LIST_STR_MAP.fieldOf("OR")
                        .xmap(x -> new When(Optional.of(x), Optional.empty(), Optional.empty()), x -> x.or.orElseThrow()).codec(),
                Codec.either(
                        LIST_STR_MAP.fieldOf("AND")
                             .xmap(x -> new When(Optional.empty(), Optional.of(x), Optional.empty()), x -> x.and.orElseThrow()).codec(),
                        STR_MAP.xmap(x -> new When(Optional.empty(), Optional.empty(), Optional.of(x)), x -> x.base.orElseThrow()))
                ).xmap(x -> x.left().orElseGet(() -> x.right().orElseThrow().left().orElseGet(x.right().get().right()::get)),

                    x -> x.or.isPresent() ? Either.left(x)
                            : x.and.isPresent() ? Either.right(Either.left(x)) : Either.right(Either.right(x))
                );
    }
}
