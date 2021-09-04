package eu.pb4.polymer;

import eu.pb4.polymer.other.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * General use case utils that can be useful in multiple situations
 */
public class PolymerUtils {
    public static String ID = "polymer";
    /**
     * Returns player if it's known to polymer (otherwise null!)
     */
    @Nullable
    public static ServerPlayerEntity getPlayer() {
        ServerPlayerEntity player = PacketContext.get().getTarget();

        if (player == null && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            player = ClientUtils.getPlayer();
        }

        return player;
    }

    public static boolean isSingleplayer() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return false;
        } else {
            return ClientUtils.isSingleplayer();
        }
    }
}
