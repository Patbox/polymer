package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.resourcepack.api.WritableAsset;

import java.util.ArrayList;
import java.util.List;

public record FontAsset(List<FontProviderEntry> providers) implements WritableAsset.Json  {
    public static final Codec<FontAsset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FontProviderEntry.CODEC.listOf().fieldOf("providers").forGetter(FontAsset::providers)
    ).apply(instance,  FontAsset::new));

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static FontAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<FontProviderEntry> providers = new ArrayList<>();

        private Builder() {}

        public FontAsset.Builder add(FontProvider provider) {
            this.providers.add(new FontProviderEntry(provider));
            return this;
        }

        public FontAsset.Builder add(FontProvider provider, FontProviderFilter filter) {
            this.providers.add(new FontProviderEntry(provider, filter));
            return this;
        }

        public FontAsset.Builder add(FontProvider.Builder provider) {
            this.providers.add(new FontProviderEntry(provider.build()));
            return this;
        }

        public FontAsset.Builder add(FontProvider.Builder provider, FontProviderFilter filter) {
            this.providers.add(new FontProviderEntry(provider.build(), filter));
            return this;
        }

        public FontAsset.Builder add(FontProviderEntry entry) {
            this.providers.add(entry);
            return this;
        }

        public FontAsset build() {
            return new FontAsset(providers);
        }
    }
}
