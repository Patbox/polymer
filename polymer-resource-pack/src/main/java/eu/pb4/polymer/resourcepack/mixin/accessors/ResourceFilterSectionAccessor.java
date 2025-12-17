package eu.pb4.polymer.resourcepack.mixin.accessors;

import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.util.IdentifierPattern;

@Mixin(ResourceFilterSection.class)
public interface ResourceFilterSectionAccessor {
    @Accessor
    static Codec<ResourceFilterSection> getCODEC() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    List<IdentifierPattern> getBlockList();
}
