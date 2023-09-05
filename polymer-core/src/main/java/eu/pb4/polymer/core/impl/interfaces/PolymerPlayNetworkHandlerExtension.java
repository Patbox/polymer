package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.block.BlockMapper;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface PolymerPlayNetworkHandlerExtension extends PolymerCommonNetworkHandlerExtension {
    boolean polymer$advancedTooltip();
    void polymer$setAdvancedTooltip(boolean value);

    BlockMapper polymer$getBlockMapper();
    void polymer$setBlockMapper(BlockMapper mapper);

    static PolymerPlayNetworkHandlerExtension of(ServerPlayerEntity player) {
        return (PolymerPlayNetworkHandlerExtension) player.networkHandler;
    }

    static PolymerPlayNetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (PolymerPlayNetworkHandlerExtension) handler;
    }

    void polymer$delayAfterSequence(Runnable runnable);
}
