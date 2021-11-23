package eu.pb4.polymer.api.utils;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import eu.pb4.polymer.mixin.entity.ServerWorldAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

/**
 * General use case utils that can be useful in multiple situations
 */
public final class PolymerUtils {
    private PolymerUtils() {
    }

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
     * Schedules a packet sending
     *
     * @param handler  used for packet sending
     * @param packet   sent packet
     * @param duration time (in ticks) waited before packet is send
     */
    public static void schedulePacket(ServerPlayNetworkHandler handler, Packet<?> packet, int duration) {
        ((PolymerNetworkHandlerExtension) handler).polymer_schedulePacket(packet, duration);
    }

    /**
     * Resends world to player. It's useful to run this after player changes resource packs
     */
    public static void reloadWorld(ServerPlayerEntity player) {
        PolymerSyncUtils.synchronizePolymerRegistries(player.networkHandler);
        player.networkHandler.sendPacket(new InventoryS2CPacket(0, 0, player.playerScreenHandler.getStacks(), player.playerScreenHandler.getCursorStack()));

        for (var e : ((ServerWorldAccessor) player.getWorld()).polymer_getEntityManager().getLookup().iterate()) {
            var tracker = ((ThreadedAnvilChunkStorageAccessor) player.getWorld().getChunkManager().threadedAnvilChunkStorage).polymer_getEntityTrackers().get(e.getId());
            tracker.stopTracking(player);
            tracker.updateTrackedStatus(player);
        }
    }

    /**
     * Resends inventory to player
     */
    public static void reloadInventory(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(new InventoryS2CPacket(0, 0, player.playerScreenHandler.getStacks(), player.playerScreenHandler.getCursorStack()));
    }

    /**
     * Returns current TooltipContext of player,
     */
    public static TooltipContext getTooltipContext(@Nullable ServerPlayerEntity player) {
        return player != null && player.networkHandler instanceof PolymerNetworkHandlerExtension h && h.polymer_advancedTooltip() ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL;
    }

    /**
     * Returns list of ItemGroups accessible by player
     */
    public static List<PolymerItemGroup> getItemGroups(ServerPlayerEntity player) {
        var list = new ArrayList<PolymerItemGroup>();

        for (var group : InternalServerRegistry.ITEM_GROUPS) {
            if (group.shouldSyncWithPolymerClient(player)) {
                list.add(group);
            }
        }

        var sync = new PolymerItemGroup.ItemGroupListBuilder() {
            @Override
            public void add(PolymerItemGroup group) {
                list.add(group);
            }

            @Override
            public void remove(PolymerItemGroup group) {
                list.remove(group);
            }
        };

        PolymerItemGroup.LIST_EVENT.invoke((x) -> x.onItemGroupSync(player, sync));

        return list;
    }

    /**
     * Creates SkullOwner NbtCompound from provided skin value
     *
     * @param value Skin value
     * @return NbtCompound representing SkullOwner
     */
    public static NbtCompound createSkullOwner(String value) {
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound data = new NbtCompound();
        NbtList textures = new NbtList();
        textures.add(data);

        data.putString("Value", value);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        skullOwner.putIntArray("Id", new int[]{0, 0, 0, 0});

        return skullOwner;
    }
}
