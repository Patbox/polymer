package eu.pb4.polymer.core.api.utils;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.mixin.block.packet.ThreadedAnvilChunkStorageAccessor;
import eu.pb4.polymer.core.mixin.entity.ServerWorldAccessor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.resource.featuretoggle.FeatureFlag;
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
import java.util.*;

/**
 * General use case utils that can be useful in multiple situations
 */
public final class PolymerUtils {
    public static final String ID = "polymer";
    public static final String NO_TEXTURE_HEAD_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=";
    private static final Set<FeatureFlag> ENABLED_FEATURE_FLAGS = new HashSet<>();

    private PolymerUtils() {
    }

    public static String getVersion() {
        return CommonImpl.VERSION;
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

    public static void addClientEnabledFeatureFlags(FeatureFlag... flags) {
        ENABLED_FEATURE_FLAGS.addAll(List.of(flags));
    }

    public static Collection<FeatureFlag> getClientEnabledFeatureFlags() {
        return ENABLED_FEATURE_FLAGS;
    }

    /**
     * Schedules a packet sending
     *
     * @param handler  used for packet sending
     * @param packet   sent packet
     * @param duration time (in ticks) waited before packet is send
     */
    public static void schedulePacket(ServerPlayNetworkHandler handler, Packet<?> packet, int duration) {
        ((PolymerPlayNetworkHandlerExtension) handler).polymer$schedulePacket(packet, duration);
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

            for (var e : ((ServerWorldAccessor) player.getWorld()).polymer_getEntityManager().getLookup().iterate()) {
                var tracker = tacsAccess.polymer$getEntityTrackers().get(e.getId());
                if (tracker != null) {
                    tracker.stopTracking(player);
                }
            }


            player.getChunkFilter().forEach((chunkPos) -> {
                var chunk = world.getChunk(chunkPos.x, chunkPos.z);
                player.networkHandler.chunkDataSender.unload(player, chunk.getPos());
                player.networkHandler.chunkDataSender.add(chunk);
            });


        });
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
        return createSkullOwner(value, null);
    }

    /**
     * Creates SkullOwner NbtCompound from provided skin value
     *
     * @param value Skin value
     * @return NbtCompound representing SkullOwner
     */
    public static NbtCompound createSkullOwner(String value, String signature) {
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound data = new NbtCompound();
        NbtList textures = new NbtList();
        textures.addElement(0, data);
        data.putString("Value", value);
        if (signature != null) {
            data.putString("Signature", signature);
        }
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        skullOwner.putIntArray("Id", new int[]{0, 0, 0, 0});

        return skullOwner;
    }

    /**
     * With 1.20.2, client logs errors when signature is missing, which might cause a lot of spam in some cases.
     * This method will be un-deprecated if the issue gets fixed.
     * <p>
     * <a href="https://bugs.mojang.com/browse/MC-264966">MC-264966</a>
     */
    @Deprecated
    public static ItemStack createPlayerHead(String value) {
        return createPlayerHead(value, null);
    }

    public static ItemStack createPlayerHead(String value, String signature) {
        var stack = new ItemStack(Items.PLAYER_HEAD);
        stack.getOrCreateNbt().put("SkullOwner", createSkullOwner(value, signature));
        return stack;
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
        return obj instanceof PolymerObject || (obj instanceof ItemStack stack && PolymerItemUtils.isPolymerServerItem(stack)) || (obj instanceof EntityType<?> type && PolymerEntityUtils.isRegisteredEntityType(type)) || (obj instanceof BlockEntityType<?> typeBE && PolymerBlockUtils.isPolymerBlockEntityType(typeBE)) || (obj instanceof VillagerProfession villagerProfession && PolymerEntityUtils.getPolymerProfession(villagerProfession) != null);

    }

    public static boolean hasResourcePack(@Nullable ServerPlayerEntity player, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(player, uuid);
    }
}
