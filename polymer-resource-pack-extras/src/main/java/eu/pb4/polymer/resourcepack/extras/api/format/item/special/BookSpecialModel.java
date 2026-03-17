package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record BookSpecialModel(float openAngle, float page1, float page2) implements SpecialModel {
    public static final MapCodec<BookSpecialModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("open_angle").forGetter(BookSpecialModel::openAngle),
            Codec.FLOAT.fieldOf("page_1").forGetter(BookSpecialModel::page1),
            Codec.FLOAT.fieldOf("page_2").forGetter(BookSpecialModel::page2)
            ).apply(instance, BookSpecialModel::new)
    );

    @Override
    public MapCodec<? extends SpecialModel> codec() {
        return CODEC;
    }
}
