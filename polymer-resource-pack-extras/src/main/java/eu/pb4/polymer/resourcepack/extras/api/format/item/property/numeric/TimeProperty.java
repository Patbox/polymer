package eu.pb4.polymer.resourcepack.extras.api.format.item.property.numeric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record TimeProperty(boolean wobble, Source source) implements NumericProperty {
    public static final MapCodec<TimeProperty> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.BOOL.optionalFieldOf("wobble", true).forGetter(TimeProperty::wobble),
                            Source.CODEC.fieldOf("source").forGetter(TimeProperty::source)
                    )
                    .apply(instance, TimeProperty::new)
    );

    @Override
    public MapCodec<TimeProperty> codec() {
        return CODEC;
    }

    public enum Source implements StringRepresentable {
        RANDOM("random"),
        DAYTIME("daytime"),
        MOON_PHASE("moon_phase");

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);
        private final String name;

        Source(final String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
