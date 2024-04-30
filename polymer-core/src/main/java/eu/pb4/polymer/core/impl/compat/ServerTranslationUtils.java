package eu.pb4.polymer.core.impl.compat;


import eu.pb4.polymer.common.impl.CommonImplUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.server.translations.api.Localization;

@ApiStatus.Internal
public class ServerTranslationUtils {
    public static final boolean IS_PRESENT;
    public static Text parseFor(ServerPlayNetworkHandler handler, Text text) {
        if (IS_PRESENT && !CommonImplUtils.isMainPlayer(handler.player)) {
            return Localization.text(text, handler.player);
        } else {
            return text;
        }
    }

    public static ItemStack parseFor(ServerPlayNetworkHandler handler, ItemStack stack) {
        stack = stack.copy();

        if (IS_PRESENT && !CommonImplUtils.isMainPlayer(handler.player)) {
            stack.apply(DataComponentTypes.ITEM_NAME, null, x -> x != null ? parseFor(handler, x) : null);
            stack.apply(DataComponentTypes.CUSTOM_NAME, null, x -> x != null ? parseFor(handler, x) : null);
            stack.apply(DataComponentTypes.LORE, null, x -> x != null ? new LoreComponent(x.lines()
                    .stream().map(y -> parseFor(handler, y)).toList()) : null);
        }
        return stack;
    }

    static {
        var present = FabricLoader.getInstance().isModLoaded("server_translations_api");

        if (present) {
            try {
                present &= FabricLoader.getInstance().getModContainer("server_translations_api").get().getMetadata().getVersion().compareTo(Version.parse("2.0.0-")) != -1;
            } catch (Throwable e) {
                present = false;
            }
        }

        IS_PRESENT = present;
    }
}
