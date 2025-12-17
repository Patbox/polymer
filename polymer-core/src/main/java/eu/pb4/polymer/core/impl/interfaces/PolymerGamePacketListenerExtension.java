package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.block.BlockMapper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolymerGamePacketListenerExtension extends PolymerCommonPacketListenerExtension {
    boolean polymer$advancedTooltip();
    void polymer$setAdvancedTooltip(boolean value);

    BlockMapper polymer$getBlockMapper();
    void polymer$setBlockMapper(BlockMapper mapper);

    static PolymerGamePacketListenerExtension of(ServerPlayer player) {
        return (PolymerGamePacketListenerExtension) player.connection;
    }

    static PolymerGamePacketListenerExtension of(ServerGamePacketListenerImpl handler) {
        return (PolymerGamePacketListenerExtension) handler;
    }

    void polymer$delayAfterSequence(Runnable runnable);
}
