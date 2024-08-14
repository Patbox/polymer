package eu.pb4.polymer.resourcepack.mixin.accessors;

import com.mojang.serialization.Codec;
import net.minecraft.resource.metadata.PackOverlaysMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PackOverlaysMetadata.class)
public interface PackOverlaysMetadataAccessor {
    @Accessor
    static Codec<PackOverlaysMetadata> getCODEC() {
        throw new UnsupportedOperationException();
    }
}
