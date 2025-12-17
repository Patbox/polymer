package eu.pb4.polymer.resourcepack.extras.api.format.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public record SoundDefinition(Type type, Identifier name, float volume, float pitch, int weight, boolean stream, int attenuationDistance, boolean preload) {
    public static final Codec<SoundDefinition> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StringRepresentable.fromEnum(Type::values).optionalFieldOf("type", Type.FILE).forGetter(SoundDefinition::type),
            Identifier.CODEC.fieldOf("name").forGetter(SoundDefinition::name),
            Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(SoundDefinition::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(SoundDefinition::pitch),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(SoundDefinition::weight),
            Codec.BOOL.optionalFieldOf("stream", false).forGetter(SoundDefinition::stream),
            Codec.INT.optionalFieldOf("attenuation_distance", 16).forGetter(SoundDefinition::attenuationDistance),
            Codec.BOOL.optionalFieldOf("preload", false).forGetter(SoundDefinition::stream)
    ).apply(instance, SoundDefinition::new));

    public static final Codec<SoundDefinition> CODEC = Codec.withAlternative(FULL_CODEC, Identifier.CODEC.xmap(SoundDefinition::new, SoundDefinition::name));

    public SoundDefinition(Type type, Identifier name, float volume, float pitch, int weight, boolean stream, int attenuationDistance) {
        this(type, name, volume, pitch, weight, stream, attenuationDistance, false);
    }

    public SoundDefinition(Type type, Identifier name, float volume, float pitch, int weight, int attenuationDistance) {
        this(type, name, volume, pitch, weight, false, attenuationDistance, false);
    }

    public SoundDefinition(Type type, Identifier name, float volume, float pitch, int weight) {
        this(type, name, volume, pitch, weight, false, 16, false);
    }

    public SoundDefinition(Type type, Identifier name, float volume, float pitch) {
        this(type, name, volume, pitch, 1, false, 16, false);
    }

    public SoundDefinition(Type type, Identifier name, float volume) {
        this(type, name, volume, 1, 1, false, 16, false);
    }

    public SoundDefinition(Type type, Identifier name) {
        this(type, name, 1, 1, 1, false, 16, false);
    }

    public SoundDefinition(Identifier name, float volume, float pitch, int weight, boolean stream, int attenuationDistance) {
        this(Type.FILE, name, volume, pitch, weight, stream, attenuationDistance, false);
    }

    public SoundDefinition(Identifier name, float volume, float pitch, int weight, int attenuationDistance) {
        this(Type.FILE, name, volume, pitch, weight, false, attenuationDistance, false);
    }

    public SoundDefinition(Identifier name, float volume, float pitch, int weight) {
        this(Type.FILE, name, volume, pitch, weight, false, 16, false);
    }

    public SoundDefinition(Identifier name, float volume, float pitch) {
        this(Type.FILE, name, volume, pitch, 1, false, 16, false);
    }

    public SoundDefinition(Identifier name, float volume) {
        this(Type.FILE, name, volume, 1, 1, false, 16, false);
    }

    public SoundDefinition(Identifier name) {
        this(Type.FILE, name, 1, 1, 1, false, 16, false);
    }

    public static Builder builder(Identifier identifier) {
        return new Builder(identifier);
    }

    public static class Builder {
        private final Identifier name;
        private Type type = Type.FILE;
        private float volume = 1;
        private float pitch = 1;
        private int weight = 1;
        private boolean stream = false;
        private int attenuationDistance = 16;
        private boolean preload = false;

        private Builder(Identifier name) {
            this.name = name;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder volume(float volume) {
            this.volume = volume;
            return this;
        }

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        public Builder attenuationDistance(int attenuationDistance) {
            this.attenuationDistance = attenuationDistance;
            return this;
        }

        public Builder preload(boolean preload) {
            this.preload = preload;
            return this;
        }

        public SoundDefinition build() {
            return new SoundDefinition(this.type, this.name, this.volume, this.pitch, this.weight, this.stream, this.attenuationDistance, this.preload);
        }
    }


    public enum Type implements StringRepresentable {
        FILE("file"),
        EVENT("event");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
