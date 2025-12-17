package eu.pb4.polymer.resourcepack.extras.api.format.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record ModelTransformation(Vec3 rotation, Vec3 translation, Vec3 scale) {
    public static final Codec<ModelTransformation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.optionalFieldOf("rotation", Vec3.ZERO).forGetter(ModelTransformation::rotation),
            Vec3.CODEC.optionalFieldOf("translation", Vec3.ZERO).forGetter(ModelTransformation::translation),
            Vec3.CODEC.optionalFieldOf("scale", Vec3.ZERO).forGetter(ModelTransformation::scale)
    ).apply(instance, ModelTransformation::new));
}
