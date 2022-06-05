package eu.pb4.polymer.api.utils;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.other.PolymerTooltipContext;
import eu.pb4.polymer.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import eu.pb4.polymer.mixin.entity.ServerWorldAccessor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * General use case utils that can be useful in multiple situations
 */
public final class PolymerUtils {
    private PolymerUtils() {
    }
    private final static String SAFE_CLIENT_SHA1 = "a984bc5036a9f0ace8da1040614996abcda5f2ad";
    private final static String SAFE_CLIENT_URL = "https://launcher.mojang.com/v1/objects/" + SAFE_CLIENT_SHA1 + "/client.jar";

    public static final String ID = "polymer";
    public static final String NO_TEXTURE_HEAD_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=";

    public static String getVersion() {
        return PolymerImpl.VERSION;
    }

    public static String getMainModuleId() {
        return PolymerImpl.MOD_ID;
    }

    public static String getMainModuleName() {
        return PolymerImpl.NAME;
    }

    /**
     * Returns player if it's known to polymer (otherwise null!)
     */
    @Nullable
    public static ServerPlayerEntity getPlayer() {
        ServerPlayerEntity player = PacketContext.get().getTarget();

        if (player == null) {
            player = PolymerImplUtils.getPlayer();

            if (player == null && PolymerImpl.IS_CLIENT) {
                player = ClientUtils.getPlayer();
            }
        }

        return player;
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

    @Deprecated
    public static boolean isOnClientSide() {
        return isOnClientThread();
    }

    public static boolean isOnPlayerNetworking() {
        if (!PolymerImpl.IS_CLIENT) {
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
        player.server.execute(() -> {
            PolymerSyncUtils.synchronizePolymerRegistries(player.networkHandler);
            player.networkHandler.sendPacket(new InventoryS2CPacket(0, 0, player.playerScreenHandler.getStacks(), player.playerScreenHandler.getCursorStack()));

            var world = player.getWorld();
            var tacsAccess = ((ThreadedAnvilChunkStorageAccessor) player.getWorld().getChunkManager().threadedAnvilChunkStorage);
            int dist = tacsAccess.getWatchDistance();
            int playerX = player.getWatchedSection().getX();
            int playerZ = player.getWatchedSection().getZ();

            for (var e : ((ServerWorldAccessor) player.getWorld()).polymer_getEntityManager().getLookup().iterate()) {
                var tracker = tacsAccess.polymer_getEntityTrackers().get(e.getId());
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

            PolymerNetworkHandlerExtension.of(player.networkHandler).polymer_delayAction("polymer:reload/send_chunks/0", 1, () -> nestedSend(player, 0, toSend));
        });
    }

    private static void nestedSend(ServerPlayerEntity player, int iteration, List<WorldChunk> chunks) {
        var tacsAccess = ((ThreadedAnvilChunkStorageAccessor) player.getWorld().getChunkManager().threadedAnvilChunkStorage);

        var iterator = chunks.listIterator();
        int pos = 0;
        while (iterator.hasNext() && pos < 15) {
            var chunk = iterator.next();
            pos++;
            iterator.remove();
            tacsAccess.polymer_sendChunkDataPackets(player, new MutableObject<>(), chunk);
        }
        if (chunks.size() != 0) {
            int finalIteration = iteration + 1;
            PolymerNetworkHandlerExtension.of(player.networkHandler).polymer_delayAction("polymer:reload/send_chunks/" + finalIteration, 1, () -> nestedSend(player, finalIteration, chunks));
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
        return player != null && player.networkHandler instanceof PolymerNetworkHandlerExtension h && h.polymer_advancedTooltip() ? PolymerTooltipContext.ADVANCED : PolymerTooltipContext.NORMAL;
    }

    /**
     * Returns list of ItemGroups accessible by player
     */
    public static List<PolymerItemGroup> getItemGroups(ServerPlayerEntity player) {
        var list = new LinkedHashSet<PolymerItemGroup>();

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

        return new ArrayList<>(list);
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

    /**
     * Allows to execute code with selected player being returned for {@link PolymerUtils#getPlayer()}
     * calls. Useful for custom packets using writeItemStack and similar methods.
     *
     * @param player
     * @param runnable
     */
    public static void executeWithPlayerContext(ServerPlayerEntity player, Runnable runnable) {
        var oldPlayer = PolymerImplUtils.getPlayer();
        var oldTarget = PacketContext.get().getTarget();

        PolymerImplUtils.setPlayer(player);
        PacketContext.setReadContext(player.networkHandler);

        runnable.run();

        PolymerImplUtils.setPlayer(oldPlayer);
        PacketContext.setReadContext(oldTarget != null ? oldTarget.networkHandler : null);
    }

    @Nullable
    public static ZipFile getClientJar() {
        try {
            Path clientJarPath;
            if (!PolymerImpl.IS_CLIENT) {
                clientJarPath = PolymerImpl.getGameDir().resolve("polymer_cache/client_jars/" + SAFE_CLIENT_SHA1 + ".jar");
            } else {
                var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                clientJarPath = Path.of(clientFile);
            }

            if (!clientJarPath.toFile().exists()) {
                Files.createDirectories(clientJarPath.getParent());
                PolymerImpl.LOGGER.info("Downloading vanilla client jar...");
                URL url = new URL(SAFE_CLIENT_URL);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                Files.copy(is, clientJarPath);
            }

            return new ZipFile(clientJarPath.toFile());
        } catch (Exception e) {
            PolymerImpl.LOGGER.error("Couldn't retrieve client jar!", e);
            return null;
        }
    }

    public static boolean isServerOnly(Object obj) {
        return obj instanceof PolymerObject
                || (obj instanceof EntityType<?> type && PolymerEntityUtils.isRegisteredEntityType(type))
                || (obj instanceof BlockEntityType<?> typeBE && PolymerBlockUtils.isRegisteredBlockEntity(typeBE))
                || (obj instanceof VillagerProfession villagerProfession && PolymerEntityUtils.getPolymerProfession(villagerProfession) != null);

    }
}
