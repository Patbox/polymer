package eu.pb4.polymer.mixin.client.item;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(Item.class)
public class ItemMixin {
    @Redirect(method = "byRawId", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/DefaultedRegistry;get(I)Ljava/lang/Object;"))
    private static Object polymer$replaceForModCompat(DefaultedRegistry instance, int index) {
        return InternalClientRegistry.decodeItem(index);
    }
}
