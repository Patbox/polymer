package eu.pb4.polymer.resourcepack.extras.api.format.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record ModelElement(Vec3 from, Vec3 to, Map<Direction, Face> faces, Optional<Rotation> rotation,
                           boolean shade, int lightEmission) {
    public static final Codec<ModelElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("from").forGetter(ModelElement::from),
            Vec3.CODEC.fieldOf("to").forGetter(ModelElement::to),
            SortedMapCodec.of(Direction.CODEC, Face.CODEC).optionalFieldOf("faces", Map.of()).forGetter(ModelElement::faces),
            Rotation.CODEC.optionalFieldOf("rotation").forGetter(ModelElement::rotation),
            Codec.BOOL.optionalFieldOf("shade", true).forGetter(ModelElement::shade),
            ExtraCodecs.intRange(0, 15).optionalFieldOf("light_emission", 0).forGetter(ModelElement::lightEmission)
    ).apply(instance, ModelElement::new));

    public ModelElement(Vec3 from, Vec3 to, Map<Direction, Face> faces, Optional<Rotation> rotation,
                        boolean shade) {
        this(from, to, faces, rotation, shade, 0);
    }
    public ModelElement(Vec3 from, Vec3 to, Map<Direction, Face> faces, Optional<Rotation> rotation) {
        this(from, to, faces, rotation, true, 0);
    }

    public ModelElement(Vec3 from, Vec3 to, Map<Direction, Face> faces) {
        this(from, to, faces, Optional.empty(), true, 0);
    }

    public record Rotation(Vec3 origin, Direction.Axis axis, float angle, boolean rescale) {
        public static final Codec<Rotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.optionalFieldOf("origin", Vec3.ZERO).forGetter(Rotation::origin),
                Direction.Axis.CODEC.fieldOf("axis").forGetter(Rotation::axis),
                Codec.FLOAT.fieldOf("angle").forGetter(Rotation::angle),
                Codec.BOOL.optionalFieldOf("rescale", false).forGetter(Rotation::rescale)
        ).apply(instance, Rotation::new));

        public Rotation(Vec3 origin, Direction.Axis axis, float angle) {
            this(origin, axis, angle, false);
        }

        public Rotation(Direction.Axis axis, float angle) {
            this(Vec3.ZERO, axis, angle, false);
        }
    }

    public record Face(List<Float> uv, String texture, Optional<Direction> cullface, int rotation, int tintIndex) {
        public static final Codec<Face> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Codec.FLOAT, 4, 4).optionalFieldOf("uv", List.of()).forGetter(Face::uv),
                Codec.STRING.optionalFieldOf("texture", "").forGetter(Face::texture),
                Direction.CODEC.optionalFieldOf("cullface").forGetter(Face::cullface),
                Codec.INT.optionalFieldOf("rotation", 0).forGetter(Face::rotation),
                Codec.INT.optionalFieldOf("tintindex", -1).forGetter(Face::tintIndex)
        ).apply(instance, Face::new));

        public Face {
            if (uv.size() != 4 && !uv.isEmpty()) {
                throw new IllegalArgumentException("uv needs to have either 4 elements or be empty");
            }
        }

        public Face(List<Float> uv, String texture, Optional<Direction> cullface, int rotation) {
            this(uv, texture, cullface, rotation, -1);
        }

        public Face(List<Float> uv, String texture, Optional<Direction> cullface) {
            this(uv, texture, cullface, 0, -1);
        }

        public Face(List<Float> uv, String texture) {
            this(uv, texture, Optional.empty(), 0, -1);
        }

        public Face(String texture) {
            this(List.of(), texture, Optional.empty(), 0, -1);
        }
    }

    public static Builder builder(Vec3 from, Vec3 to) {
        return new Builder(from, to);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        private final Vec3 from;
        private final Vec3 to;
        private final Map<Direction, Face> faces = new EnumMap<>(Direction.class);
        private Optional<Rotation> rotation = Optional.empty();
        private boolean shade = true;
        private int lightEmission = 0;

        private Builder(Vec3 from, Vec3 to) {
            this.from = from;
            this.to = to;
        }

        public Builder rotation(Vec3 origin, Direction.Axis axis, float angle, boolean rescale) {
            return this.rotation(new Rotation(origin, axis, angle, rescale));
        }

        public Builder rotation(Vec3 origin, Direction.Axis axis, float angle) {
            return this.rotation(new Rotation(origin, axis, angle));
        }

        public Builder rotation(Direction.Axis axis, float angle) {
            return this.rotation(new Rotation(axis, angle));
        }

        public Builder face(Direction direction, Face face) {
            this.faces.put(direction, face);
            return this;
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture, Direction cullFace, int rotation, int tint) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture, Optional.ofNullable(cullFace), rotation, tint));
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture, Direction cullFace, int rotation) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture, Optional.ofNullable(cullFace), rotation));
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture, Direction cullFace) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture, Optional.ofNullable(cullFace)));
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture));
        }

        public Builder face(Direction direction, String texture, Direction cullFace, int rotation, int tint) {
            return this.face(direction, new Face(List.of(), texture, Optional.ofNullable(cullFace), rotation, tint));
        }

        public Builder face(Direction direction, String texture, int rotation, int tint) {
            return this.face(direction, new Face(List.of(), texture, Optional.empty(), rotation, tint));
        }

        public Builder face(Direction direction, String texture, int rotation) {
            return this.face(direction, new Face(List.of(), texture, Optional.empty(), rotation));
        }

        public Builder face(Direction direction, String texture) {
            return this.face(direction, new Face(texture));
        }

        public Builder rotation(Rotation rotation) {
            this.rotation = Optional.ofNullable(rotation);
            return this;
        }

        public Builder shade(boolean shade) {
            this.shade = shade;
            return this;
        }

        public Builder lightEmission(int lightEmission) {
            this.lightEmission = Mth.clamp(lightEmission, 0, 15);
            return this;
        }


        public ModelElement build() {
            return new ModelElement(from, to, new EnumMap<>(this.faces), this.rotation, this.shade, this.lightEmission);
        }
    }
}
