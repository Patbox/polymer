package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.api.x.BlockMapper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolymerNetworkHandlerExtension {
    boolean polymer_hasResourcePack();

    void polymer_setResourcePack(boolean value);

    void polymer_schedulePacket(Packet<?> packet, int duration);

    boolean polymer_hasPolymer();
    String polymer_version();
    int polymer_protocolVersion();

    void polymer_setVersion(String version);

    long polymer_lastPacketUpdate(String identifier);
    void polymer_savePacketTime(String identifier);

    int polymer_getSupportedVersion(String identifier);
    void polymer_setSupportedVersion(String identifier, int i);
    Object2IntMap<String> polymer_getSupportMap();

    boolean polymer_advancedTooltip();
    void polymer_setAdvancedTooltip(boolean value);

    void polymer_delayAction(String identifier, int delay, Runnable action);

    BlockMapper polymer_getBlockMapper();
    void polymer_setBlockMapper(BlockMapper mapper);

    static PolymerNetworkHandlerExtension of(ServerPlayerEntity player) {
        return (PolymerNetworkHandlerExtension) player.networkHandler;
    }

    static PolymerNetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (PolymerNetworkHandlerExtension) handler;
    }

    void polymer_setIgnoreNext();

    void polymer_resetSupported();
}
