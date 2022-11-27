package eu.pb4.polymer.core.mixin.client.rendering;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @ModifyVariable(method = "<init>", at = @At("STORE"), ordinal = 0, require = 0)
    private Identifier polymer$skipPolymerItems(Identifier identifier) {
        return Registries.ITEM.get(identifier) instanceof PolymerItem item && !PolymerKeepModel.is(item) ? new Identifier("air") : identifier;
    }
}
