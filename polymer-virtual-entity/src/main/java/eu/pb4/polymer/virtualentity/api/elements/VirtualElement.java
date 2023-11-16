package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface VirtualElement {
    IntList getEntityIds();

    @ApiStatus.OverrideOnly
    void setHolder(@Nullable ElementHolder holder);

    @Nullable
    ElementHolder getHolder();

    Vec3d getOffset();
    void setOffset(Vec3d vec3d);

    void startWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer);
    void stopWatching(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> packetConsumer);
    void notifyMove(Vec3d oldPos, Vec3d currentPos, Vec3d delta);
    void tick();

    InteractionHandler getInteractionHandler(ServerPlayerEntity player);

    default void setInitialPosition(Vec3d newPos) {
    }

    interface InteractionHandler {
        InteractionHandler EMPTY = new InteractionHandler() {};

        static InteractionHandler redirect(Entity redirectedEntity) {
            return new InteractionHandler() {
                @Override
                public void interact(ServerPlayerEntity player, Hand hand) {
                    player.networkHandler.onPlayerInteractEntity(PlayerInteractEntityC2SPacket.interact(redirectedEntity, player.isSneaking(), hand));
                }

                @Override
                public void interactAt(ServerPlayerEntity player, Hand hand, Vec3d pos) {
                    player.networkHandler.onPlayerInteractEntity(PlayerInteractEntityC2SPacket.interactAt(redirectedEntity, player.isSneaking(), hand, pos));
                }

                @Override
                public void attack(ServerPlayerEntity player) {
                    player.networkHandler.onPlayerInteractEntity(PlayerInteractEntityC2SPacket.attack(redirectedEntity, player.isSneaking()));
                }
            };
        }

        default void interact(ServerPlayerEntity player, Hand hand) {};
        default void interactAt(ServerPlayerEntity player, Hand hand, Vec3d pos) {};
        default void attack(ServerPlayerEntity player) {};
    }
}
