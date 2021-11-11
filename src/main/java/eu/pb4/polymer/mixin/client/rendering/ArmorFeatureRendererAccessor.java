package eu.pb4.polymer.mixin.client.rendering;

import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ArmorFeatureRenderer.class)
public interface ArmorFeatureRendererAccessor{
    @Accessor
    static Map<String, Identifier> getARMOR_TEXTURE_CACHE() {
        throw new UnsupportedOperationException();
    }
}
