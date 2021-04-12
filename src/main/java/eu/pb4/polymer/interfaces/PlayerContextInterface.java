package eu.pb4.polymer.interfaces;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerContextInterface {
    void setPolymerPlayer(ServerPlayerEntity player);
    ServerPlayerEntity getPolymerPlayer();
}
