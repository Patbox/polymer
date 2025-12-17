package eu.pb4.polymer.resourcepack.extras.api.format.atlas;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.WritableAsset;
import eu.pb4.polymer.resourcepack.mixin.accessors.IdentifierPatternAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.util.IdentifierPattern;

public record AtlasAsset(List<AtlasSource> sources) implements WritableAsset.Json {
    public static final Codec<AtlasAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AtlasSource.CODEC.listOf().fieldOf("sources").forGetter(AtlasAsset::sources)
    ).apply(instance, AtlasAsset::new));

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static AtlasAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<AtlasSource> sources = new ArrayList<>();

        private Builder() {}

        public Builder single(Identifier resource, Identifier sprite) {
            return this.add(new SingleAtlasSource(resource, Optional.ofNullable(sprite)));
        }

        public Builder single(Identifier resource) {
            return this.add(new SingleAtlasSource(resource, Optional.empty()));
        }

        public Builder directory(String source, String prefix) {
            return this.add(new DirectoryAtlasSource(source, prefix));
        }

        public Builder filter(IdentifierPattern entry) {
            return this.add(new FilterAtlasSource(entry));
        }

        public Builder filter(Pattern namespace, Pattern path) {
            return this.add(new FilterAtlasSource(IdentifierPatternAccessor.create(Optional.ofNullable(namespace), Optional.ofNullable(path))));
        }

        public Builder filterNamespace(Pattern namespace) {
            return this.add(new FilterAtlasSource(IdentifierPatternAccessor.create(Optional.ofNullable(namespace), Optional.empty())));
        }

        public Builder filterPath(Pattern path) {
            return this.add(new FilterAtlasSource(IdentifierPatternAccessor.create(Optional.empty(), Optional.ofNullable(path))));
        }

        public Builder unstitch(Identifier resource, double divisorX, double divisorY, Consumer<RegionConsumer> regionConsumer) {
            var arr = new ArrayList<UnstitchAtlasSource.Region>();
            regionConsumer.accept(arr::add);
            return this.add(new UnstitchAtlasSource(resource, arr, divisorX, divisorY));
        }

        public Builder unstitch(Identifier resource, Consumer<RegionConsumer> regionConsumer) {
            var arr = new ArrayList<UnstitchAtlasSource.Region>();
            regionConsumer.accept(arr::add);
            return this.add(new UnstitchAtlasSource(resource, arr));
        }

        public Builder palettedPermutations(List<Identifier> textures, Identifier paletteKey, Map<String, Identifier> permutations) {
            return this.add(new PalettedPermutationsAtlasSource(textures, paletteKey, permutations));
        }

        public Builder palettedPermutations(PalettedPermutationsAtlasSource.Builder builder) {
            return this.add(builder.build());
        }

        public Builder palettedPermutations(Identifier paletteKey, Consumer<PalettedPermutationsAtlasSource.Builder> builderConsumer) {
            var builder = PalettedPermutationsAtlasSource.builder(paletteKey);
            builderConsumer.accept(builder);
            return this.add(builder.build());
        }

        public Builder add(AtlasSource source) {
            this.sources.add(source);
            return this;
        }

        public AtlasAsset build() {
            return new AtlasAsset(sources);
        }

        public interface RegionConsumer extends Consumer<UnstitchAtlasSource.Region> {
            default void accept(Identifier sprite, double x, double y, double width, double height) {
                this.accept(new UnstitchAtlasSource.Region(sprite, x, y, width, height));
            }
        }
    }
}
