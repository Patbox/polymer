package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientUtils {
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

        return stack.getItem() instanceof PolymerItem item && !PolymerKeepModel.is(item) ? item.getPolymerItemStack(stack, PolymerUtils.getTooltipContext(ClientUtils.getPlayer()), ClientUtils.getPlayer()) : stack;
    }
}
