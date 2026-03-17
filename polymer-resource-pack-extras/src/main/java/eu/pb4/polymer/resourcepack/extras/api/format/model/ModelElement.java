package eu.pb4.polymer.resourcepack.extras.api.format.model;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.common.impl.SortedMapCodec;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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

    public static Builder builder(Vec3 from, Vec3 to) {
        return new Builder(from, to);
    }

    public record Rotation(Vector3fc origin, RotationValue value, boolean rescale) {
        public static final Codec<Rotation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.VECTOR3F.optionalFieldOf("origin", new Vector3f()).forGetter(Rotation::origin),
                Codec.mapEither(
                                RecordCodecBuilder.<SingleAxis>mapCodec(instance1 -> instance1.group(
                                                Direction.Axis.CODEC.fieldOf("axis").forGetter(SingleAxis::axis),
                                                Codec.FLOAT.fieldOf("angle").forGetter(SingleAxis::angle)
                                        ).apply(instance1, SingleAxis::new)
                                ),
                                RecordCodecBuilder.<Euler>mapCodec(instance1 -> instance1.group(
                                                Codec.FLOAT.optionalFieldOf("x", 0f).forGetter(Euler::x),
                                                Codec.FLOAT.optionalFieldOf("y", 0f).forGetter(Euler::y),
                                                Codec.FLOAT.optionalFieldOf("z", 0f).forGetter(Euler::z)
                                        ).apply(instance1, Euler::new)
                                )
                        ).xmap(x -> x.map(Function.<RotationValue>identity(), Function.identity()),
                                x -> x instanceof Euler euler ? Either.right(euler) : Either.left((SingleAxis) x))
                        .forGetter(Rotation::value),
                Codec.BOOL.optionalFieldOf("rescale", false).forGetter(Rotation::rescale)
        ).apply(instance, Rotation::new));

        public Rotation(Vector3fc origin, Vector3fc euler, boolean rescale) {
            this(origin, new Euler(euler.x(), euler.y(), euler.z()), rescale);
        }

        public Rotation(Vector3fc origin, Direction.Axis axis, float angle, boolean rescale) {
            this(origin, new SingleAxis(axis, angle), rescale);
        }

        public sealed interface RotationValue permits Euler, SingleAxis {
        }

        public record Euler(float x, float y, float z) implements RotationValue {
            public static Euler ZERO = new Euler(0, 0, 0);
        }

        public record SingleAxis(Direction.Axis axis, float angle) implements RotationValue {
        }
    }

    public record Face(List<Float> uv, String texture, Optional<Direction> cullface, Quadrant rotation, int tintIndex) {
        public static final Codec<Face> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.list(Codec.FLOAT, 4, 4).optionalFieldOf("uv", List.of()).forGetter(Face::uv),
                Codec.STRING.optionalFieldOf("texture", "").forGetter(Face::texture),
                Direction.CODEC.optionalFieldOf("cullface").forGetter(Face::cullface),
                Quadrant.CODEC.optionalFieldOf("rotation", Quadrant.R0).forGetter(Face::rotation),
                Codec.INT.optionalFieldOf("tintindex", -1).forGetter(Face::tintIndex)
        ).apply(instance, Face::new));

        public Face {
            if (uv.size() != 4 && !uv.isEmpty()) {
                throw new IllegalArgumentException("uv needs to have either 4 elements or be empty");
            }
        }

        public Face(List<Float> uv, String texture, Optional<Direction> cullface, Quadrant rotation) {
            this(uv, texture, cullface, rotation, -1);
        }

        public Face(List<Float> uv, String texture, Optional<Direction> cullface) {
            this(uv, texture, cullface, Quadrant.R0, -1);
        }

        public Face(List<Float> uv, String texture) {
            this(uv, texture, Optional.empty(), Quadrant.R0, -1);
        }

        public Face(String texture) {
            this(List.of(), texture, Optional.empty(), Quadrant.R0, -1);
        }
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

        public Builder rotation(Vector3fc origin, Direction.Axis axis, float angle, boolean rescale) {
            return this.rotation(new Rotation(origin, axis, angle, rescale));
        }

        public Builder rotation(Vector3fc origin, Direction.Axis axis, float angle) {
            return this.rotation(new Rotation(origin, axis, angle, false));
        }

        public Builder rotation(Direction.Axis axis, float angle) {
            return this.rotation(new Rotation(new Vector3f(), axis, angle, false));
        }

        public Builder rotation(Vector3fc origin, Vector3fc rotation, boolean rescale) {
            return this.rotation(new Rotation(origin, rotation, rescale));
        }

        public Builder rotation(Vector3fc origin, Vector3fc rotation) {
            return this.rotation(new Rotation(origin, rotation, false));
        }

        public Builder rotation(Vector3fc rotation) {
            return this.rotation(new Rotation(new Vector3f(), rotation, false));
        }

        public Builder face(Direction direction, Face face) {
            this.faces.put(direction, face);
            return this;
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture, Direction cullFace, Quadrant rotation, int tint) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture, Optional.ofNullable(cullFace), rotation, tint));
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture, Direction cullFace, Quadrant rotation) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture, Optional.ofNullable(cullFace), rotation));
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture, Direction cullFace) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture, Optional.ofNullable(cullFace)));
        }

        public Builder face(Direction direction, float u1, float v1, float u2, float v2, String texture) {
            return this.face(direction, new Face(FloatList.of(u1, v1, u2, v2), texture));
        }

        public Builder face(Direction direction, String texture, Direction cullFace, Quadrant rotation, int tint) {
            return this.face(direction, new Face(List.of(), texture, Optional.ofNullable(cullFace), rotation, tint));
        }

        public Builder face(Direction direction, String texture, Quadrant rotation, int tint) {
            return this.face(direction, new Face(List.of(), texture, Optional.empty(), rotation, tint));
        }

        public Builder face(Direction direction, String texture, Quadrant rotation) {
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
