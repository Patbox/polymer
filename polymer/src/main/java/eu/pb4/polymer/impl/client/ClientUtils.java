package eu.pb4.polymer.impl.client;

import eu.pb4.polymer.api.client.PolymerKeepModel;
import eu.pb4.polymer.api.item.PolymerItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static final String PACK_ID = "$polymer-resources";

    public static boolean isResourcePackLoaded() {
        return MinecraftClient.getInstance().getResourcePackManager().getEnabledNames().contains(PACK_ID);
    }

    public static boolean isSingleplayer() {
        return MinecraftClient.getInstance().getServer() != null;
    }

    public static ServerPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().player != null
                ? MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid())
                : null;
    }

    public static boolean isClientThread() {
        return MinecraftClient.getInstance().isOnThread();
    }

    public static ItemStack getRenderingStack(ItemStack stack) {
        if (stack.getItem() instanceof VirtualClientItem virtualItem) {
            var og = stack;

            stack = virtualItem.getPolymerEntry().visualStack().copy();
            stack.setCount(og.getCount());
        }

        return stack.getItem() instanceof PolymerItem item && !PolymerKeepModel.is(item) ? item.getPolymerItemStack(stack, ClientUtils.getPlayer()) : stack;
    }
}
