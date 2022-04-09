package eu.pb4.polymer.api.client;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientEntityExtension;
import eu.pb4.polymer.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.impl.networking.ClientPackets;
import eu.pb4.polymer.impl.networking.ServerPackets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * General utilities while dealing with client side integrations
 */
@Environment(EnvType.CLIENT)
public final class PolymerClientUtils {
    private PolymerClientUtils() {
    }

    private static final Map<Identifier, String> MAP_S2C = new HashMap<>();
    private static final Map<Identifier, Identifier> MAP_C2S = new HashMap<>();

    /**
     * This event is run after receiving server handshake packet
     */
    public static final SimpleEvent<Runnable> ON_HANDSHAKE = new SimpleEvent<>();
    /**
     * This event is run after clearing registries
     */
    public static final SimpleEvent<Runnable> ON_CLEAR = new SimpleEvent<>();
    /**
     * This event ir run before Polymer registry sync
     */
    public static final SimpleEvent<Runnable> ON_SYNC_STARTED = new SimpleEvent<>();
    /**
     * This event ir run after Polymer registry sync
     */
    public static final SimpleEvent<Runnable> ON_SYNC_FINISHED = new SimpleEvent<>();
    /**
     * This event ir run on rebuild of creative search
     */
    public static final SimpleEvent<Runnable> ON_SEARCH_REBUILD = new SimpleEvent<>();
    /**
     * This event is before client asks for sync request
     */
    public static final SimpleEvent<Runnable> ON_SYNC_REQUEST = new SimpleEvent<>();
    /**
     * This event is run after receiving an Polymer block update
     */
    public static final SimpleEvent<BiConsumer<BlockPos, ClientPolymerBlock.State>> ON_BLOCK_UPDATE = new SimpleEvent<>();
    /**
     * This event is run when Polymer functionality is disabled (good for clearing)
    */
    public static final SimpleEvent<Runnable> ON_DISABLE = new SimpleEvent<>();


    public static ClientPolymerBlock.State getPolymerStateAt(BlockPos pos) {
        return InternalClientRegistry.getBlockAt(pos);
    }

    public static void setPolymerStateAt(BlockPos pos, ClientPolymerBlock.State state) {
        InternalClientRegistry.setBlockAt(pos, state);
    }

    @Nullable
    public static ClientPolymerEntityType getEntityType(Entity entity) {
        return InternalClientRegistry.ENTITY_TYPES.get(((ClientEntityExtension) entity).polymer_getId());
    }

    public static String getServerVersion() {
        return InternalClientRegistry.serverVersion;
    }

    public static boolean isEnabled() {
        return InternalClientRegistry.enabled;
    }

    public static boolean sendPacket(ClientPlayNetworkHandler player, Identifier identifier, PacketByteBuf packetByteBuf) {
        var packetName = MAP_C2S.get(identifier);
        if (packetName == null) {
            packetName = PolymerImplUtils.id("custom/" + identifier.getNamespace() + "/" + identifier.getPath());
            MAP_C2S.put(identifier, packetName);
        }
        player.sendPacket(new CustomPayloadC2SPacket(packetName, packetByteBuf));
        return true;
    }

    public static boolean registerPacketHandler(Identifier identifier, PolymerClientPacketHandler handler, int... supportedVersions) {
        if (!MAP_S2C.containsKey(identifier)) {
            var packet = "custom/" + identifier.getNamespace() + "/" + identifier.getPath();
            MAP_S2C.put(identifier, packet);

            ServerPackets.register(packet, supportedVersions);
            PolymerClientProtocolHandler.CUSTOM_PACKETS.put(packet, handler);
            return true;
        }
        return false;
    }

    public static boolean registerClientPacket(Identifier identifier, int... supportedVersions) {
        ClientPackets.register("custom/" + identifier.getNamespace() + "/" + identifier.getPath(), supportedVersions);
        return true;
    }

    public static int getSupportedVersion(Identifier identifier) {
        return InternalClientRegistry.getProtocol("custom/" + identifier.getNamespace() + "/" + identifier.getPath());
    }

    @Deprecated
    public static boolean registerPacket(Identifier identifier, PolymerClientPacketHandler handler, int... supportedVersions) {
        return registerPacketHandler(identifier, handler, supportedVersions);
    }

    public static int getBlockStateOffset() {
        return InternalClientRegistry.blockOffset != -1 ? InternalClientRegistry.blockOffset : PolymerBlockUtils.getBlockStateOffset();
    }
}
