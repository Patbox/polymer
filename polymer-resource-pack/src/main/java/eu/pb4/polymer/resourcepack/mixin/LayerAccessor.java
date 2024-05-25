package eu.pb4.polymer.resourcepack.mixin;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorMaterial.Layer.class)
public interface LayerAccessor {
    @Accessor
    Identifier getId();
}
