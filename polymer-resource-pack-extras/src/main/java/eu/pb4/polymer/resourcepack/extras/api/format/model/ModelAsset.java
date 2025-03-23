package eu.pb4.polymer.resourcepack.extras.api.format.model;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.WritableAsset;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Consumer;

public record ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, String> textures,
                         Map<ItemDisplayContext, ModelTransformation> display,
                         Optional<GuiLight> guiLight,
                         boolean ambientOcclusion) implements WritableAsset.Json {
    public static final Codec<ModelAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(ModelAsset::parent),
            ModelElement.CODEC.listOf().optionalFieldOf("elements").forGetter(ModelAsset::elements),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("textures", Map.of()).forGetter(ModelAsset::textures),
            Codec.unboundedMap(ItemDisplayContext.CODEC, ModelTransformation.CODEC).optionalFieldOf("display", Map.of()).forGetter(ModelAsset::display),
            GuiLight.CODEC.optionalFieldOf("gui_light").forGetter(ModelAsset::guiLight),
            Codec.BOOL.optionalFieldOf("ambientocclusion", true).forGetter(ModelAsset::ambientOcclusion)
    ).apply(instance, ModelAsset::new));

    public ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, String> textures,
                      Map<ItemDisplayContext, ModelTransformation> display,
                      Optional<GuiLight> guiLight) {
        this(parent, elements, textures, display, guiLight, true);
    }

    public ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, String> textures,
                      Map<ItemDisplayContext, ModelTransformation> display) {
        this(parent, elements, textures, display, Optional.empty(), true);
    }

    public ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, String> textures) {
        this(parent, elements, textures, Map.of(), Optional.empty(), true);
    }

    public ModelAsset(Identifier parent, Map<String, String> textures) {
        this(Optional.of(parent), Optional.empty(), textures, Map.of(), Optional.empty(), true);
    }

    public ModelAsset(List<ModelElement> elements, Map<String, String> textures) {
        this(Optional.empty(), Optional.of(elements), textures, Map.of(), Optional.empty(), true);
    }

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static ModelAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        private Optional<Identifier> parent = Optional.empty();
        private Optional<List<ModelElement>> elements = Optional.empty();
        private final Map<String, String> textures = new HashMap<>();
        private final Map<ItemDisplayContext, ModelTransformation> display = new HashMap<>();
        private Optional<GuiLight> guiLight = Optional.empty();
        private boolean ambientOcclusion = true;
        private Builder() {}

        public Builder parent(Identifier parent) {
            this.parent = Optional.ofNullable(parent);
            return this;
        }

        public Builder transformation(ItemDisplayContext context, ModelTransformation transformation) {
            this.display.put(context, transformation);
            return this;
        }

        public Builder withElements() {
            if (this.elements.isEmpty()) {
                this.elements = Optional.of(new ArrayList<>());
            }
            return this;
        }

        public Builder withElements(List<ModelElement> elements) {
            this.elements = Optional.ofNullable(elements);
            return this;
        }

        public Builder element(ModelElement element) {
            this.withElements();
            //noinspection OptionalGetWithoutIsPresent
            this.elements.get().add(element);
            return this;
        }

        public Builder elements(Collection<ModelElement> element) {
            this.withElements();
            //noinspection OptionalGetWithoutIsPresent
            this.elements.get().addAll(element);
            return this;
        }

        public Builder element(Vec3d from, Vec3d to, Consumer<ModelElement.Builder> builderConsumer) {
            var builder = ModelElement.builder(from, to);
            builderConsumer.accept(builder);
            return this.element(builder.build());
        }

        public Builder texture(String key, String value) {
            this.textures.put(key, value);
            return this;
        }

        public Builder texture(Map<String, String> textures) {
            this.textures.putAll(textures);
            return this;
        }

        public Builder guiLight(GuiLight guiLight) {
            this.guiLight = Optional.ofNullable(guiLight);
            return this;
        }

        public Builder ambientOcclusion(boolean ambientOcclusion) {
            this.ambientOcclusion = ambientOcclusion;
            return this;
        }


        public ModelAsset build() {
            return new ModelAsset(this.parent, this.elements.map(ArrayList::new), new HashMap<>(this.textures), new HashMap<>(this.display), this.guiLight, this.ambientOcclusion);
        }
    }
}
