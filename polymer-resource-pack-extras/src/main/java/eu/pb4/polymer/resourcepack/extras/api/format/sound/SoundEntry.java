package eu.pb4.polymer.resourcepack.extras.api.format.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;

public record SoundEntry(boolean replace, List<SoundDefinition> sounds, Optional<String> subtitle) {
    public static final Codec<SoundEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(SoundEntry::replace),
            SoundDefinition.CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(SoundEntry::sounds),
            Codec.STRING.optionalFieldOf("subtitle").forGetter(SoundEntry::subtitle)
    ).apply(instance, SoundEntry::new));

    public SoundEntry(boolean replace, List<SoundDefinition> sounds, String subtitle) {
        this(replace, sounds, Optional.ofNullable(subtitle));
    }

    public SoundEntry(List<SoundDefinition> sounds, String subtitle) {
        this(false, sounds, Optional.ofNullable(subtitle));
    }

    public SoundEntry(boolean replace, List<SoundDefinition> sounds) {
        this(replace, sounds, Optional.empty());
    }

    public SoundEntry(List<SoundDefinition> sounds) {
        this(false, sounds, Optional.empty());
    }

    public SoundEntry(boolean replace, SoundDefinition sound, String subtitle) {
        this(replace, List.of(sound), Optional.ofNullable(subtitle));
    }

    public SoundEntry(SoundDefinition sound, String subtitle) {
        this(false, List.of(sound), Optional.ofNullable(subtitle));
    }

    public SoundEntry(boolean replace, SoundDefinition sound) {
        this(replace, List.of(sound), Optional.empty());
    }

    public SoundEntry(SoundDefinition sound) {
        this(false, List.of(sound), Optional.empty());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean replace = false;
        private final List<SoundDefinition> sounds = new ArrayList<>();
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<String> subtitle = Optional.empty();
        private Builder() {}

        public Builder sounds(List<SoundDefinition> sounds) {
            this.sounds.addAll(sounds);
            return this;
        }

        public Builder sound(SoundDefinition sound) {
            this.sounds.add(sound);
            return this;
        }

        public Builder sound(SoundDefinition.Builder builder) {
            this.sounds.add(builder.build());
            return this;
        }

        public Builder sound(Identifier identifier, Consumer<SoundDefinition.Builder> builderConsumer) {
            var x = SoundDefinition.builder(identifier);
            builderConsumer.accept(x);
            this.sounds.add(x.build());
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = Optional.ofNullable(subtitle);
            return this;
        }

        public Builder replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public SoundEntry build() {
            return new SoundEntry(this.replace, new ArrayList<>(sounds), this.subtitle);
        }
    }
}
