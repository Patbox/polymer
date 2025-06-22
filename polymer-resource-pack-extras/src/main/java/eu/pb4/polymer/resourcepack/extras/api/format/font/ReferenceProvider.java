package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.Map;

public record ReferenceProvider(Identifier id) implements FontProvider {
    public static final MapCodec<ReferenceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ReferenceProvider::id)
    ).apply(instance, ReferenceProvider::new));

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }
}
