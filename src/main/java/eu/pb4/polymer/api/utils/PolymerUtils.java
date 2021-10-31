package eu.pb4.polymer.api.utils;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.networking.ServerPacketBuilders;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * General use case utils that can be useful in multiple situations
 */
public class PolymerUtils {
    public static final String ID = "polymer";

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

    /**
     * Returns true, if server is running in singleplayer
     */
    public static boolean isSingleplayer() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return false;
        } else {
            return ClientUtils.isSingleplayer();
        }
    }

    /**
     * Returns true, if code is running on logical client side (not server/singleplayer server)
     */
    public static boolean isOnClientSide() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return false;
        } else {
            return ClientUtils.isClientSide();
        }
    }

    public static boolean isOnPlayerNetworking() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            return getPlayer() != null;
        } else {
            return getPlayer() != null || ClientUtils.isSingleplayer();
        }
    }

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizePolymerRegistries(ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.createSyncPackets(handler);
    }

    /**
     * Resends synchronization packets to player if their client supports that
     */
    public static void synchronizeCreativeTabs(ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.createCreativeTabSync(handler);
    }

    /**
     * Sends/Updates Creative tab for player
     */
    public static void sendCreativeTab(PolymerItemGroup group, ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.removeItemGroup(group, handler);
        ServerPacketBuilders.syncItemGroup(group, handler);
    }

    /**
     * Removes creative tab from player
     */
    public static void removeCreativeTab(PolymerItemGroup group, ServerPlayNetworkHandler handler) {
        ServerPacketBuilders.removeItemGroup(group, handler);
    }

    /**
     * Schedules a packet sending
     * @param handler used for packet sending
     * @param packet sent packet
     * @param duration time (in ticks) waited before packet is send
     */
    public static void schedulePacket(ServerPlayNetworkHandler handler, Packet<?> packet, int duration) {
        ((PolymerNetworkHandlerExtension) handler).polymer_schedulePacket(packet, duration);
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }
}
