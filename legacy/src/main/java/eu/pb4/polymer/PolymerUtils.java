package eu.pb4.polymer;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Use {@link eu.pb4.polymer.api.utils.PolymerUtils} instead
 */
@Deprecated
public class PolymerUtils {
    public static final String ID = eu.pb4.polymer.api.utils.PolymerUtils.ID;
    public static final int BLOCK_STATE_OFFSET = PolymerBlockUtils.BLOCK_STATE_OFFSET;

    @Nullable
    public static ServerPlayerEntity getPlayer() {
        return eu.pb4.polymer.api.utils.PolymerUtils.getPlayer();
    }

    public static boolean isSingleplayer() {
        return eu.pb4.polymer.api.utils.PolymerUtils.isSingleplayer();
    }

    public static boolean isOnClientSide() {
        return eu.pb4.polymer.api.utils.PolymerUtils.isOnClientSide();
    }

    public static boolean isOnPlayerNetworking() {
        return eu.pb4.polymer.api.utils.PolymerUtils.isOnPlayerNetworking();
    }

    public static void schedulePacket(ServerPlayNetworkHandler handler, Packet<?> packet, int duration) {
        eu.pb4.polymer.api.utils.PolymerUtils.schedulePacket(handler, packet, duration);
    }
}
