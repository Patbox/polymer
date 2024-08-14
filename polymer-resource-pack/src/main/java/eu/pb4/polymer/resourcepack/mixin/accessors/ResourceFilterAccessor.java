package eu.pb4.polymer.resourcepack.mixin.accessors;

import com.mojang.serialization.Codec;
import net.minecraft.resource.metadata.BlockEntry;
import net.minecraft.resource.metadata.ResourceFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ResourceFilter.class)
public interface ResourceFilterAccessor {
    @Accessor
    static Codec<ResourceFilter> getCODEC() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    List<BlockEntry> getBlocks();
}
