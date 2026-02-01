package eu.pb4.polymer.resourcepack.extras.api.format.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record BitmapProvider(Identifier file, List<String> chars, int ascent, int height) implements FontProvider {
    public static final MapCodec<BitmapProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("file").forGetter(BitmapProvider::file),
            Codec.STRING.listOf().fieldOf("chars").forGetter(BitmapProvider::chars),
            Codec.INT.fieldOf("ascent").forGetter(BitmapProvider::ascent),
            Codec.INT.optionalFieldOf("height", 8).forGetter(BitmapProvider::height)
    ).apply(instance, BitmapProvider::new));


    public BitmapProvider(Identifier file, List<String> chars, int ascent) {
        this(file, chars, ascent, 8);
    }

    public static BitmapProvider.Builder builder(Identifier file) {
        return new BitmapProvider.Builder(file);
    }

    @Override
    public MapCodec<? extends FontProvider> codec() {
        return CODEC;
    }

    public static class Builder implements FontProvider.Builder {
        private final List<String> chars = new ArrayList<>();
        private final Identifier file;
        private int ascent = 7;
        private int height = 8;

        private Builder(Identifier file) {
            this.file = file;
        }

        public BitmapProvider.Builder chars(String string) {
            this.chars.add(string);
            return this;
        }

        public BitmapProvider.Builder height(int height) {
            this.height = height;
            return this;
        }

        public BitmapProvider.Builder ascent(int ascent) {
            this.ascent = ascent;
            return this;
        }

        public BitmapProvider build() {
            return new BitmapProvider(this.file, this.chars, this.ascent, this.height);
        }
    }
}
