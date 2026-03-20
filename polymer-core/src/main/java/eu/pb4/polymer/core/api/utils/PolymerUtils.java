package eu.pb4.polymer.core.api.utils;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.ScopedOverride;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerGamePacketListenerExtension;
import eu.pb4.polymer.core.impl.networking.PacketPatcher;
import eu.pb4.polymer.core.mixin.StaticAccessor;
import eu.pb4.polymer.core.mixin.block.packet.ServerMapAccessor;
import eu.pb4.polymer.core.mixin.entity.ServerLevelAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

/**
 * General use case utils that can be useful in multiple situations
 */
public final class PolymerUtils {
    public static final String NO_TEXTURE_HEAD_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=";
    private static final Set<FeatureFlag> ENABLED_FEATURE_FLAGS = new HashSet<>();
    private static final Set<ResourceKey<? extends Registry<?>>> SERVER_ONLY_REGISTRIES = new HashSet<>();

    private PolymerUtils() {
    }

    public static String getVersion() {
        return CommonImpl.VERSION;
    }

    public static void addClientEnabledFeatureFlags(FeatureFlag... flags) {
        ENABLED_FEATURE_FLAGS.addAll(List.of(flags));
    }

    public static Collection<FeatureFlag> getClientEnabledFeatureFlags() {
        return ENABLED_FEATURE_FLAGS;
    }

    public static ScopedOverride ignorePlaySoundExclusion() {
        if (PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION.get() != null) {
            return ScopedOverride.NO_OP;
        }
        PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION.set(Unit.INSTANCE);
        return PolymerImplUtils.IGNORE_PLAY_SOUND_EXCLUSION::remove;
    }

    /**
     * Schedules a packet sending
     *
     * @param handler  used for packet sending
     * @param packet   sent packet
     * @param duration time (in ticks) waited before packet is send
     */
    public static void schedulePacket(ServerGamePacketListenerImpl handler, Packet<?> packet, int duration) {
        ((PolymerGamePacketListenerExtension) handler).polymer$schedulePacket(packet, duration);
    }

    /**
     * Resends world to player. It's useful to run this after player changes resource packs
     */
    public static void reloadWorld(ServerPlayer player) {
        player.level().getServer().execute(() -> {
            PolymerImplUtils.IS_RELOADING_WORLD.set(Unit.INSTANCE);
            try {
                player.containerMenu.sendAllDataToRemote();

                var world = player.level();
                var tacsAccess = ((ServerMapAccessor) world.getChunkSource().chunkMap);

                for (var e : ((ServerLevelAccessor) world).polymer_getEntityManager().getEntityGetter().getAll()) {
                    var tracker = tacsAccess.polymer$getEntityTrackers().get(e.getId());
                    if (tracker != null) {
                        tracker.removePlayer(player);
                    }
                }


                player.getChunkTrackingView().forEach((chunkPos) -> {
                    var chunk = world.getChunk(chunkPos.x(), chunkPos.z());
                    player.connection.chunkSender.dropChunk(player, chunk.getPos());
                    player.connection.chunkSender.markChunkPendingToSend(chunk);
                });
            } catch (Throwable e) {
                PolymerImpl.LOGGER.warn("Failed to reload player's world view!", e);
            }

            PolymerImplUtils.IS_RELOADING_WORLD.remove();
        });
    }

    /**
     * Resends inventory to player
     */
    public static void reloadInventory(ServerPlayer player) {
        player.containerMenu.sendAllDataToRemote();
    }

    /**
     * Returns current TooltipContext of player,
     */
    public static TooltipFlag getTooltipType(@Nullable ServerPlayer player) {
        return PolymerImplUtils.getTooltipContext(player);
    }

    /**
     * Returns current TooltipContext of player,
     */
    public static TooltipFlag getCreativeTooltipType(@Nullable ServerPlayer player) {
        return PolymerImplUtils.getTooltipContext(player).withCreative();
    }


    public static ResolvableProfile createProfileComponent(String value) {
        return createProfileComponent(value, null);
    }

    public static ResolvableProfile createProfileComponent(String value, @Nullable String signature) {
        var profile = new PropertyMap(ImmutableMultimap.of("textures", new Property("textures", value, signature)));
        return ResolvableProfile.createResolved(new GameProfile(Util.NIL_UUID, "", profile));
    }

    public static ResolvableProfile createProfileComponent(PlayerSkin.Patch override) {
        return StaticAccessor.createStatic(Either.right(ResolvableProfile.Partial.EMPTY), override);
    }


    public static ItemStack createPlayerHead(String value) {
        return createPlayerHead(value, null);
    }

    public static ItemStack createPlayerHead(String value, String signature) {
        var stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponents.PROFILE, createProfileComponent(value, signature));
        return stack;
    }

    public static Level getFakeWorld() {
        return PolymerCommonUtils.getFakeWorld();
    }

    @Nullable
    public static Path getClientJar() {
        return PolymerCommonUtils.getClientJar();
    }

    public static <T> boolean isServerOnly(Registry<T> registry, T obj) {
        return RegistrySyncUtils.isServerEntry(registry, obj);
    }

    public static boolean hasResourcePack(@Nullable PacketContextProvider provider, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(provider, uuid);
    }


    public static Packet<?> replacePacket(ServerCommonPacketListenerImpl handler, Packet<?> packet) {
        return PacketPatcher.replace(handler, packet);
    }

    public static boolean shouldPreventPacket(ServerCommonPacketListenerImpl handler, Packet<?> packet) {
        return PacketPatcher.prevent(handler, packet);
    }

    public static boolean isServerOnlyRegistry(ResourceKey<? extends Registry<?>> key) {
        return SERVER_ONLY_REGISTRIES.contains(key);
    }

    public static void markAsServerOnlyRegistry(ResourceKey<? extends Registry<?>> key) {
        if (key.identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return;
        }
        SERVER_ONLY_REGISTRIES.add(key);
    }
}
