package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record SpaceProvider(Map<String, Float> advances) implements FontProvider {
    public static final MapCodec<SpaceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT).fieldOf("advances").forGetter(SpaceProvider::advances)
    ).apply(instance, SpaceProvider::new));

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder implements FontProvider.Builder {
        private final Object2FloatMap<String> map = new Object2FloatOpenHashMap<>();

        private Builder() {
        }

        public Builder add(String character, int size) {
            this.map.put(character, size);
            return this;
        }

        public Builder add(int character, int size) {
            this.map.put(Character.toString(character), size);
            return this;
        }

        public Builder add(char character, int size) {
            this.map.put(Character.toString(character), size);
            return this;
        }

        public SpaceProvider build() {
            return new SpaceProvider(this.map);
        }
    }
}
