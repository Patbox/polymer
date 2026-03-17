package eu.pb4.polymer.resourcepack.extras.api.format.model;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import eu.pb4.polymer.resourcepack.api.WritableAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public record ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements,
                         Map<String, TextureSlot> textures,
                         Map<ItemDisplayContext, ModelTransformation> display,
                         Optional<GuiLight> guiLight,
                         boolean ambientOcclusion) implements WritableAsset.Json {
    public static final Codec<ModelAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(ModelAsset::parent),
            ModelElement.CODEC.listOf().optionalFieldOf("elements").forGetter(ModelAsset::elements),
            SortedMapCodec.of(Codec.STRING, TextureSlot.CODEC).optionalFieldOf("textures", Map.of()).forGetter(ModelAsset::textures),
            SortedMapCodec.of(ItemDisplayContext.CODEC, ModelTransformation.CODEC).optionalFieldOf("display", Map.of()).forGetter(ModelAsset::display),
            GuiLight.CODEC.optionalFieldOf("gui_light").forGetter(ModelAsset::guiLight),
            Codec.BOOL.optionalFieldOf("ambientocclusion", true).forGetter(ModelAsset::ambientOcclusion)
    ).apply(instance, ModelAsset::new));

    public ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, TextureSlot> textures,
                      Map<ItemDisplayContext, ModelTransformation> display,
                      Optional<GuiLight> guiLight) {
        this(parent, elements, textures, display, guiLight, true);
    }

    public ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, TextureSlot> textures,
                      Map<ItemDisplayContext, ModelTransformation> display) {
        this(parent, elements, textures, display, Optional.empty(), true);
    }

    public ModelAsset(Optional<Identifier> parent, Optional<List<ModelElement>> elements, Map<String, TextureSlot> textures) {
        this(parent, elements, textures, Map.of(), Optional.empty(), true);
    }

    public ModelAsset(Identifier parent, Map<String, TextureSlot> textures) {
        this(Optional.of(parent), Optional.empty(), textures, Map.of(), Optional.empty(), true);
    }

    public ModelAsset(List<ModelElement> elements, Map<String, TextureSlot> textures) {
        this(Optional.empty(), Optional.of(elements), textures, Map.of(), Optional.empty(), true);
    }

    public static ModelAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public sealed interface TextureSlot extends StringRepresentable permits TextureReference, TextureValue {
        Codec<TextureSlot> CODEC = Codec.either(
                Codec.STRING.comapFlatMap((string) -> {
                    if (!string.startsWith("#")) {
                        return DataResult.error(() -> "Not a reference");
                    } else {
                        return DataResult.success(new TextureReference(string.substring(1)));
                    }
                }, ref -> "#" + ref.ref),
                Codec.withAlternative(RecordCodecBuilder.create(instance -> instance.group(
                        Identifier.CODEC.fieldOf("sprite").forGetter(TextureValue::sprite),
                        Codec.BOOL.optionalFieldOf("force_translucent", false).forGetter(TextureValue::forceTranslucent)
                ).apply(instance, TextureValue::new)), Identifier.CODEC, x -> new TextureValue(x, false))
        ).xmap(x -> x.map(Function.identity(), Function.identity()),
                x -> x instanceof TextureReference ref ? Either.left(ref) : Either.right((TextureValue) x));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        private final Map<String, TextureSlot> textures = new HashMap<>();
        private final Map<ItemDisplayContext, ModelTransformation> display = new HashMap<>();
        private Optional<Identifier> parent = Optional.empty();
        private Optional<List<ModelElement>> elements = Optional.empty();
        private Optional<GuiLight> guiLight = Optional.empty();
        private boolean ambientOcclusion = true;

        private Builder() {
        }

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

        public Builder element(Vec3 from, Vec3 to, Consumer<ModelElement.Builder> builderConsumer) {
            var builder = ModelElement.builder(from, to);
            builderConsumer.accept(builder);
            return this.element(builder.build());
        }

        public Builder textureReference(String key, String value) {
            this.textures.put(key, new TextureReference(value));
            return this;
        }

        public Builder texture(String key, Identifier value) {
            this.textures.put(key, new TextureValue(value, false));
            return this;
        }

        public Builder texture(String key, Identifier value, boolean forceTransparency) {
            this.textures.put(key, new TextureValue(value, forceTransparency));
            return this;
        }

        public Builder texture(Map<String, TextureSlot> textures) {
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

    public record TextureReference(String ref) implements TextureSlot {
        @Override
        public String getSerializedName() {
            return "#" + ref;
        }
    }

    public record TextureValue(Identifier sprite, boolean forceTranslucent) implements TextureSlot {
        @Override
        public String getSerializedName() {
            return sprite + (forceTranslucent ? "?forceTranslucent" : "");
        }
    }
}
