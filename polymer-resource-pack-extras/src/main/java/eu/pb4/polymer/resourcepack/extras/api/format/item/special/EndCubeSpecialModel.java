package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record EndCubeSpecialModel(Type effect) implements SpecialModel {
    public static final MapCodec<EndCubeSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Type.CODEC.fieldOf("effect").forGetter(EndCubeSpecialModel::effect)
            ).apply(instance, EndCubeSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }

    public enum Type implements StringRepresentable {
        PORTAL("portal"),
        GATEWAY("gateway");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String name;

        Type(final String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }
    }
}
