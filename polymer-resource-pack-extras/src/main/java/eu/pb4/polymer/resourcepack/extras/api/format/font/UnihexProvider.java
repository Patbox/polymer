package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.font.UnihexFont;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;

public record UnihexProvider(Identifier hexFile, List<SizeOverride> sizeOverrides) implements FontProvider {
    public static final MapCodec<UnihexProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("hex_file").forGetter(UnihexProvider::hexFile),
            SizeOverride.CODEC.listOf().optionalFieldOf("size_overrides", List.of()).forGetter(UnihexProvider::sizeOverrides)
    ).apply(instance, UnihexProvider::new));

    public UnihexProvider(Identifier hexFile) {
        this(hexFile, List.of());
    }

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public record SizeOverride(int from, int to, int left, int right) {
        public static final Codec<SizeOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.CODEPOINT.fieldOf("from").forGetter(SizeOverride::from),
                Codecs.CODEPOINT.fieldOf("to").forGetter(SizeOverride::to),
                Codec.INT.fieldOf("left").forGetter(SizeOverride::left),
                Codec.INT.fieldOf("right").forGetter(SizeOverride::right)
        ).apply(instance, SizeOverride::new));


        public SizeOverride(String from, String to, int left, int right) {
            this(from.codePointAt(0), to.codePointAt(0), left, right);
        }
    }
}
