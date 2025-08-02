package eu.pb4.polymer.resourcepack.extras.api.format.blockstate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public record StateModelVariant(Identifier model, int x, int y, boolean uvlock, int weigth) {
    private static final Codec<StateModelVariant> BASE = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(StateModelVariant::model),
            Codec.INT.optionalFieldOf("x", 0).forGetter(StateModelVariant::x),
            Codec.INT.optionalFieldOf("y", 0).forGetter(StateModelVariant::y),
            Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(StateModelVariant::uvlock),
            Codec.INT.optionalFieldOf("weigth", 1).forGetter(StateModelVariant::weigth)
            ).apply(instance, StateModelVariant::new)
    );

    public static final Codec<List<StateModelVariant>> CODEC = Codec.withAlternative(BASE.listOf(), BASE, List::of);
    public static final Codec<Map<String, List<StateModelVariant>>> MAP = SortedMapCodec.of(Codec.STRING, CODEC);
}
