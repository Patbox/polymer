package eu.pb4.polymer.core.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ArmorTextureMetadata(int scale, int frames, int animationSpeed, boolean interpolate, int emissivity) {
    public static final ArmorTextureMetadata DEFAULT = new ArmorTextureMetadata(1,1, 0, false, 0);


    public static final Codec<ArmorTextureMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("scale", DEFAULT.scale).forGetter(ArmorTextureMetadata::scale),
            Codec.INT.optionalFieldOf("frames", DEFAULT.frames).forGetter(ArmorTextureMetadata::frames),
            Codec.INT.optionalFieldOf("speed", DEFAULT.animationSpeed).forGetter(ArmorTextureMetadata::animationSpeed),
            Codec.BOOL.optionalFieldOf("interpolate", DEFAULT.interpolate).forGetter(ArmorTextureMetadata::interpolate),
            Codec.INT.optionalFieldOf("emissivity", DEFAULT.emissivity).forGetter(ArmorTextureMetadata::emissivity)
    ).apply(instance, ArmorTextureMetadata::new));
}
