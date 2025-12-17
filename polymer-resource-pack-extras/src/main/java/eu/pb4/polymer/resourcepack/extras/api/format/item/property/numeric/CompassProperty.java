package eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record CompassProperty(boolean wobble, Target target) implements NumericProperty {
    public static final MapCodec<CompassProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("wobble", true).forGetter(CompassProperty::wobble),
            Target.CODEC.fieldOf("target").forGetter(CompassProperty::target)
    ).apply(instance, CompassProperty::new));

    @Override
    public MapCodec<CompassProperty> codec() {
        return CODEC;
    }

    public enum Target implements StringRepresentable {
        NONE("none"),
        LODESTONE("lodestone"),
        SPAWN("spawn"),
        RECOVERY("recovery");

        public static final Codec<Target> CODEC = StringRepresentable.fromEnum(Target::values);
        private final String name;

        Target(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
