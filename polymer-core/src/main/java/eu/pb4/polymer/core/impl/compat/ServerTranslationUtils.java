package eu.pb4.polymer.core.impl.compat;


import eu.pb4.polymer.common.impl.CommonImplUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.ApiStatus;
import xyz.nucleoid.server.translations.api.Localization;

@ApiStatus.Internal
public class ServerTranslationUtils {
    public static final boolean IS_PRESENT;
    public static Component parseFor(ServerGamePacketListenerImpl handler, Component text) {
        if (IS_PRESENT && !CommonImplUtils.isMainPlayer(handler.player)) {
            return Localization.text(text, handler.player);
        } else {
            return text;
        }
    }

    public static ItemStack parseFor(ServerGamePacketListenerImpl handler, ItemStack stack) {
        stack = stack.copy();

        if (IS_PRESENT && !CommonImplUtils.isMainPlayer(handler.player)) {
            stack.update(DataComponents.ITEM_NAME, null, x -> x != null ? parseFor(handler, x) : null);
            stack.update(DataComponents.CUSTOM_NAME, null, x -> x != null ? parseFor(handler, x) : null);
            stack.update(DataComponents.LORE, null, x -> x != null ? new ItemLore(x.lines()
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
