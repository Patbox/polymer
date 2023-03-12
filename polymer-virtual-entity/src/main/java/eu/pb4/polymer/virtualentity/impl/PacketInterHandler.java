package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public record PacketInterHandler(ServerPlayerEntity player, VirtualElement.InteractionHandler interactionHandler) implements PlayerInteractEntityC2SPacket.Handler {
    @Override
    public void interact(Hand hand) {
        interactionHandler.interact(player, hand);
    }

    @Override
    public void interactAt(Hand hand, Vec3d pos) {
        interactionHandler.interactAt(player, hand, pos);
    }

    @Override
    public void attack() {
        interactionHandler.attack(player);
    }
}
