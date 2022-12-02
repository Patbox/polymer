package eu.pb4.polymer.core.api.utils;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.core.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import eu.pb4.polymer.core.mixin.entity.ServerWorldAccessor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * General use case utils that can be useful in multiple situations
 */
public final class PolymerUtils {
    private PolymerUtils() {
    }

    public static final String ID = "polymer";
    public static final String NO_TEXTURE_HEAD_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=";

    public static String getVersion() {
        return PolymerImpl.VERSION;
    }

    /**
     * Returns player if it's known to polymer (otherwise null!)
     */
    @Nullable
    public static ServerPlayerEntity getPlayerContext() {
        return PolymerCommonUtils.getPlayerContext();
    }

    /**
     * Returns true, if server is running in singleplayer
     */
    public static boolean isSingleplayer() {
        if (!PolymerImpl.IS_CLIENT) {
            return false;
        } else {
            return ClientUtils.isSingleplayer();
        }
    }

    /**
     * Returns true, if code is running on logical client side (not server/singleplayer server)
     */
    public static boolean isOnClientThread() {
        if (!PolymerImpl.IS_CLIENT) {
            return false;
        } else {
            return ClientUtils.isClientThread();
        }
    }

    public static boolean isOnPlayerNetworking() {
        if (!PolymerImpl.IS_CLIENT) {
            return getPlayerContext() != null;
        } else {
            return getPlayerContext() != null || ClientUtils.isSingleplayer();
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
        ((PolymerNetworkHandlerExtension) handler).polymer$schedulePacket(packet, duration);
    }

    /**
     * Resends world to player. It's useful to run this after player changes resource packs
     */
    public static void reloadWorld(ServerPlayerEntity player) {
        player.server.execute(() -> {
            PolymerSyncUtils.synchronizePolymerRegistries(player.networkHandler);
            player.networkHandler.sendPacket(new InventoryS2CPacket(0, 0, player.playerScreenHandler.getStacks(), player.playerScreenHandler.getCursorStack()));

            var world = player.getWorld();
            var tacsAccess = ((ThreadedAnvilChunkStorageAccessor) ((ServerChunkManager) player.getWorld().getChunkManager()).threadedAnvilChunkStorage);
            int dist = tacsAccess.polymer$getWatchDistance();
            int playerX = player.getWatchedSection().getX();
            int playerZ = player.getWatchedSection().getZ();

            for (var e : ((ServerWorldAccessor) player.getWorld()).polymer_getEntityManager().getLookup().iterate()) {
                var tracker = tacsAccess.polymer$getEntityTrackers().get(e.getId());
                if (tracker != null) {
                    tracker.stopTracking(player);
                }
            }

            var toSend = new ArrayList<WorldChunk>();

            for (int x = -dist; x <= dist; x++) {
                for (int z = -dist; z <= dist; z++) {
                    var chunk = (WorldChunk) world.getChunk(x + playerX, z + playerZ, ChunkStatus.FULL, false);
                    if (chunk != null) {
                        toSend.add(chunk);
                    }
                }
            }

            PolymerNetworkHandlerExtension.of(player.networkHandler).polymer$delayAction("polymer:reload/send_chunks/0", 1, () -> nestedSend(player, 0, toSend));
        });
    }

    private static void nestedSend(ServerPlayerEntity player, int iteration, List<WorldChunk> chunks) {
        var tacsAccess = ((ThreadedAnvilChunkStorageAccessor) ((ServerChunkManager) player.getWorld().getChunkManager()).threadedAnvilChunkStorage);

        var iterator = chunks.listIterator();
        int pos = 0;
        while (iterator.hasNext() && pos < 15) {
            var chunk = iterator.next();
            pos++;
            iterator.remove();
            tacsAccess.polymer$sendChunkDataPackets(player, new MutableObject<>(), chunk);
        }
        if (chunks.size() != 0) {
            int finalIteration = iteration + 1;
            PolymerNetworkHandlerExtension.of(player.networkHandler).polymer$delayAction("polymer:reload/send_chunks/" + finalIteration, 1, () -> nestedSend(player, finalIteration, chunks));
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
        return PolymerImplUtils.getTooltipContext(player);
    }

    /**
     * Returns current TooltipContext of player,
     */
    public static TooltipContext getCreativeTooltipContext(@Nullable ServerPlayerEntity player) {
        return PolymerImplUtils.getTooltipContext(player).withCreative();
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
        textures.addElement(0, data);

        data.putString("Value", value);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        skullOwner.putIntArray("Id", new int[]{0, 0, 0, 0});

        return skullOwner;
    }

    /**
     * Allows to execute code with selected player being returned for {@link PolymerUtils#getPlayerContext()}
     * calls. Useful for custom packets using writeItemStack and similar methods.
     *
     * @param player
     * @param runnable
     */
    public static void executeWithPlayerContext(ServerPlayerEntity player, Runnable runnable) {
        PolymerCommonUtils.executeWithPlayerContext(player, runnable);
    }

    public static World getFakeWorld() {
        return PolymerCommonUtils.getFakeWorld();
    }

    @Nullable
    public static Path getClientJar() {
        return PolymerCommonUtils.getClientJar();
    }

    public static boolean isServerOnly(Object obj) {
        return obj instanceof PolymerObject
                || (obj instanceof ItemStack stack && PolymerItemUtils.isPolymerServerItem(stack))
                || (obj instanceof EntityType<?> type && PolymerEntityUtils.isRegisteredEntityType(type))
                || (obj instanceof BlockEntityType<?> typeBE && PolymerBlockUtils.isPolymerBlockEntityType(typeBE))
                || (obj instanceof VillagerProfession villagerProfession && PolymerEntityUtils.getPolymerProfession(villagerProfession) != null);

    }

    public static boolean hasResourcePack(@Nullable ServerPlayerEntity player) {
        return (player != null && ((CommonResourcePackInfoHolder) player).polymerCommon$hasResourcePack()) || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }
}
