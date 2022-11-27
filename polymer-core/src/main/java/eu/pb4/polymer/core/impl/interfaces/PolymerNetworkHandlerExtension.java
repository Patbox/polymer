package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.block.BlockMapper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolymerNetworkHandlerExtension {
    void polymer$schedulePacket(Packet<?> packet, int duration);

    boolean polymer$hasPolymer();
    String polymer$version();
    int polymer$protocolVersion();

    void polymer$setVersion(String version);

    long polymer$lastPacketUpdate(String identifier);
    void polymer$savePacketTime(String identifier);

    int polymer$getSupportedVersion(String identifier);
    void polymer$setSupportedVersion(String identifier, int i);
    Object2IntMap<String> polymer$getSupportMap();

    boolean polymer$advancedTooltip();
    void polymer$setAdvancedTooltip(boolean value);

    void polymer$delayAction(String identifier, int delay, Runnable action);

    BlockMapper polymer$getBlockMapper();
    void polymer$setBlockMapper(BlockMapper mapper);

    static PolymerNetworkHandlerExtension of(ServerPlayerEntity player) {
        return (PolymerNetworkHandlerExtension) player.networkHandler;
    }

    static PolymerNetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (PolymerNetworkHandlerExtension) handler;
    }

    void polymer$resetSupported();

    void polymer$delayAfterSequence(Runnable runnable);
}
