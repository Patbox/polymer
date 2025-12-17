package eu.pb4.polymer.common.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static final String PACK_ID = "$polymer-resources";
    public static volatile ServerPlayer backupPlayer;

    public static boolean isResourcePackLoaded() {
        return Minecraft.getInstance().getResourcePackRepository().getSelectedIds().contains(PACK_ID);
    }

    public static boolean isSingleplayer() {
        return Minecraft.getInstance().getSingleplayerServer() != null;
    }

    public static ServerPlayer getPlayer() {
        if (Minecraft.getInstance().getSingleplayerServer() != null) {
            if (Minecraft.getInstance().player != null) {
                var p = Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID());
                if (p != null) {
                    return p;
                }
            }
        }

        return backupPlayer;
    }

    public static boolean isClientThread() {
        return Minecraft.getInstance().isSameThread();
    }

    public static HolderLookup.Provider getLookup() {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess();
        }
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
}
