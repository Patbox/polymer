package eu.pb4.polymer.core.api.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.networking.PacketPatcher;
import eu.pb4.polymer.core.mixin.block.packet.ServerChunkLoadingManagerAccessor;
import eu.pb4.polymer.core.mixin.entity.ServerWorldAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
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
            PolymerImplUtils.IS_RELOADING_WORLD.set(Unit.INSTANCE);
            player.networkHandler.sendPacket(new InventoryS2CPacket(0, 0, player.playerScreenHandler.getStacks(), player.playerScreenHandler.getCursorStack()));

            var world = player.getWorld();
            var tacsAccess = ((ServerChunkLoadingManagerAccessor) ((ServerChunkManager) player.getWorld().getChunkManager()).chunkLoadingManager);

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

            PolymerImplUtils.IS_RELOADING_WORLD.remove();
        });
    }

    public static void markAsPolymer(ComponentType<?>... types) {
        PolymerItemUtils.markAsPolymer(types);
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
    public static TooltipType getTooltipType(@Nullable ServerPlayerEntity player) {
        return PolymerImplUtils.getTooltipContext(player);
    }

    /**
     * Returns current TooltipContext of player,
     */
    public static TooltipType getCreativeTooltipType(@Nullable ServerPlayerEntity player) {
        return PolymerImplUtils.getTooltipContext(player).withCreative();
    }

    public static GameProfile createSkinGameProfile(String value, @Nullable String signature) {
        var profile = new GameProfile(Util.NIL_UUID, "");
        profile.getProperties().put("textures", new Property("textures", value, signature));
        return profile;
    }


    public static ItemStack createPlayerHead(String value) {
        return createPlayerHead(value, null);
    }

    public static ItemStack createPlayerHead(String value, String signature) {
        var stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(createSkinGameProfile(value, signature)));
        return stack;
    }

    public static World getFakeWorld() {
        return PolymerCommonUtils.getFakeWorld();
    }

    @Nullable
    public static Path getClientJar() {
        return PolymerCommonUtils.getClientJar();
    }

    @SuppressWarnings("unchecked")
    public static boolean isServerOnly(Object obj) {
        return obj instanceof PolymerObject
                || (obj instanceof ItemStack stack && PolymerItemUtils.isPolymerServerItem(stack))
                || (obj instanceof EntityType<?> type && PolymerEntityUtils.isPolymerEntityType(type))
                || (obj instanceof BlockEntityType<?> typeBE && PolymerBlockUtils.isPolymerBlockEntityType(typeBE))
                || (obj instanceof RegistryEntry<?> entry && (
                        (entry.value() instanceof ArmorMaterial && PolymerItemUtils.isPolymerArmorMaterial((RegistryEntry<ArmorMaterial>) entry))
                        || (entry.value() instanceof EntityAttribute && PolymerEntityUtils.isPolymerEntityAttribute((RegistryEntry<EntityAttribute>) entry))))
                || (obj instanceof ComponentType<?> componentType && PolymerComponent.isPolymerComponent(componentType))
                || (obj instanceof VillagerProfession villagerProfession && PolymerEntityUtils.getPolymerProfession(villagerProfession) != null);
    }
    public static <T> boolean isServerOnly(Registry<T> registry, T obj) {
        return RegistrySyncUtils.isServerEntry(registry, obj) || isServerOnly(obj);
    }

    public static boolean hasResourcePack(@Nullable ServerPlayerEntity player, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(player, uuid);
    }

    public static Packet<?> replacePacket(ServerCommonNetworkHandler handler, Packet<?> packet) {
        return PacketPatcher.replace(handler, packet);
    }

    public static boolean shouldPreventPacket(ServerCommonNetworkHandler handler, Packet<?> packet) {
        return PacketPatcher.prevent(handler, packet);
    }

    public static ProfileComponent createProfileComponent(String value, @Nullable String signature) {
        return new ProfileComponent(createSkinGameProfile(value, signature));
    }

    /**
     * Use PolymerCommonUtils.executeWithNetworkingLogic
     */
    @Deprecated
    public static void executeWithPlayerContext(ServerPlayerEntity player, Runnable runnable) {
        PolymerCommonUtils.executeWithPlayerContext(player, runnable);
    }
}
