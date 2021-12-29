package eu.pb4.polymer.impl.resourcepack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ArmorTextureMetadata(int scale, int frames, int animationSpeed, boolean interpolate, int emissivity) {
    public static final Codec<ArmorTextureMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("scale", 1).forGetter(ArmorTextureMetadata::scale),
            Codec.INT.optionalFieldOf("frames", 1).forGetter(ArmorTextureMetadata::frames),
            Codec.INT.optionalFieldOf("speed", 0).forGetter(ArmorTextureMetadata::animationSpeed),
            Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(ArmorTextureMetadata::interpolate),
            Codec.INT.optionalFieldOf("emissivity", 0).forGetter(ArmorTextureMetadata::emissivity)
    ).apply(instance, ArmorTextureMetadata::new));


    public static final ArmorTextureMetadata DEFAULT = new ArmorTextureMetadata(1,1, 0, false, 0);
}
