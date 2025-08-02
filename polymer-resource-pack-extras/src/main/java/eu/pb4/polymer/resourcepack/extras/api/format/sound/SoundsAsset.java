package eu.pb4.polymer.resourcepack.extras.api.format.sound;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import eu.pb4.polymer.resourcepack.api.WritableAsset;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public record SoundsAsset(Map<String, SoundEntry> sounds) implements WritableAsset.Json {
    public static final Codec<SoundsAsset> CODEC = SortedMapCodec.of(Codec.STRING, SoundEntry.CODEC).xmap(SoundsAsset::new, SoundsAsset::sounds);

    public String toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public static SoundsAsset fromJson(String json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().getFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, SoundEntry> sounds = new HashMap<>();
        private Builder() {}

        public Builder add(String path, SoundEntry entry) {
            this.sounds.put(path, entry);
            return this;
        }

        public Builder add(String path, SoundEntry.Builder entry) {
            this.sounds.put(path, entry.build());
            return this;
        }

        public Builder add(String path, Consumer<SoundEntry.Builder> builderConsumer) {
            var b = SoundEntry.builder();
            builderConsumer.accept(b);
            this.sounds.put(path, b.build());
            return this;
        }

        public SoundsAsset build() {
            return new SoundsAsset(new HashMap<>(this.sounds));
        }

    }
}
