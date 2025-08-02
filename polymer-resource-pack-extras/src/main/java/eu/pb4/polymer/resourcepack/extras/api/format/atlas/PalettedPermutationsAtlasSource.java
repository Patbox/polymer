package eu.pb4.polymer.resourcepack.extras.api.format.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import net.minecraft.util.Identifier;

import java.util.*;

public record PalettedPermutationsAtlasSource(List<Identifier> textures, Identifier paletteKey, Map<String, Identifier> permutations) implements AtlasSource {
    public static final MapCodec<PalettedPermutationsAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(Identifier.CODEC).fieldOf("textures").forGetter(PalettedPermutationsAtlasSource::textures),
            Identifier.CODEC.fieldOf("palette_key").forGetter(PalettedPermutationsAtlasSource::paletteKey),
            SortedMapCodec.of(Codec.STRING, Identifier.CODEC).fieldOf("permutations").forGetter(PalettedPermutationsAtlasSource::permutations)
    ).apply(instance, PalettedPermutationsAtlasSource::new));

    @Override
    public MapCodec<? extends AtlasSource> codec() {
        return CODEC;
    }

    public static Builder builder(Identifier paletteKey) {
        return new Builder(paletteKey);
    }

    public static class Builder {
        private final Identifier paletteKey;
        private final List<Identifier> textures = new ArrayList<>();
        private final Map<String, Identifier> permutations = new HashMap<>();
        private Builder(Identifier paletteKey) {
            this.paletteKey = paletteKey;
        }

        public Builder texture(Identifier texture) {
            this.textures.add(texture);
            return this;
        }

        public Builder textures(Collection<Identifier> textures) {
            this.textures.addAll(textures);
            return this;
        }

        public Builder permutation(String suffix, Identifier paletteKey) {
            this.permutations.put(suffix, paletteKey);
            return this;
        }

        public Builder permutations(Map<String, Identifier> permutations) {
            this.permutations.putAll(permutations);
            return this;
        }

        public PalettedPermutationsAtlasSource build() {
            return new PalettedPermutationsAtlasSource(new ArrayList<>(this.textures), paletteKey, new HashMap<>(permutations));
        }
    }
}
