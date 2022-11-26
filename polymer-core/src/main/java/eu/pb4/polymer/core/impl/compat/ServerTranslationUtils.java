package eu.pb4.polymer.core.impl.compat;

import fr.catcore.server.translations.api.LocalizationTarget;
import fr.catcore.server.translations.api.nbt.StackNbtLocalizer;
import fr.catcore.server.translations.api.text.LocalizableText;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ServerTranslationUtils {
    public static final boolean IS_PRESENT = FabricLoader.getInstance().isModLoaded("server_translations_api");

    public static Text parseFor(ServerPlayNetworkHandler handler, Text text) {
        if (IS_PRESENT) {
            return LocalizableText.asLocalizedFor(text, (LocalizationTarget) handler.player);
        } else {
            return text;
        }
    }

    public static ItemStack parseFor(ServerPlayNetworkHandler handler, ItemStack stack) {
        if (IS_PRESENT) {
            var newStack = stack.copy();
            newStack.setNbt(StackNbtLocalizer.localize(newStack, newStack.getNbt(), (LocalizationTarget) handler.player));
            return newStack;
        }
        return stack.copy();
    }
}
