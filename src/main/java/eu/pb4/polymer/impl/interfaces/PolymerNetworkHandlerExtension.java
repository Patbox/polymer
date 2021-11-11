package eu.pb4.polymer.impl.interfaces;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
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

    long polymer_lastSyncUpdate();
    void polymer_saveSyncTime();

    int polymer_getSupportedVersion(String identifier);
    void polymer_setSupportedVersion(String identifier, int i);
    Object2IntMap<String> polymer_getSupportMap();

    boolean polymer_advancedTooltip();
    void polymer_setAdvancedTooltip(boolean value);


    static PolymerNetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (PolymerNetworkHandlerExtension) handler;
    }
}
