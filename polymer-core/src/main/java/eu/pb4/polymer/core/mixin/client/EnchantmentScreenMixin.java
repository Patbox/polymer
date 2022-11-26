package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(EnchantmentScreen.class)
public class EnchantmentScreenMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/Enchantment;byRawId(I)Lnet/minecraft/enchantment/Enchantment;"))
    private Enchantment polymer$decodeId(int id) {
        return InternalClientRegistry.decodeEnchantment(id);
    }
}
