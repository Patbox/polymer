package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public record PacketInterHandler(ServerPlayer player, VirtualElement.InteractionHandler interactionHandler) implements ServerboundInteractPacket.Handler {
    @Override
    public void onInteraction(InteractionHand hand) {
        interactionHandler.interact(player, hand);
    }

    @Override
    public void onInteraction(InteractionHand hand, Vec3 pos) {
        interactionHandler.interactAt(player, hand, pos);
    }

    @Override
    public void onAttack() {
        interactionHandler.attack(player);
    }
}
