package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @ModifyVariable(method = "<init>", at = @At("STORE"), ordinal = 0)
    private Identifier polymer_skipPolymerItems(Identifier identifier) {
        return Registry.ITEM.get(identifier) instanceof VirtualItem ? new Identifier("air") : identifier;
    }
}
