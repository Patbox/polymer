package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface VirtualElement {
    IntList getEntityIds();

    @ApiStatus.OverrideOnly
    void setHolder(@Nullable ElementHolder holder);

    @Nullable
    ElementHolder getHolder();

    Vec3 getOffset();
    void setOffset(Vec3 vec3d);
    @Nullable
    default Vec3 getOverridePos() {
        return null;
    }

    default void setOverridePos(Vec3 vec3d) {};

    default Vec3 getCurrentPos() {
        var pos = this.getOverridePos();
        return pos != null ? pos : (this.getHolder() != null ? this.getHolder().getPos().add(this.getOffset()) : Vec3.ZERO);
    }


    @Nullable
    default Vec3 getLastSyncedPos() {
        return this.getCurrentPos();
    }

    void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer);
    void stopWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer);
    void notifyMove(Vec3 oldPos, Vec3 currentPos, Vec3 delta);
    void tick();

    InteractionHandler getInteractionHandler(ServerPlayer player);

    default void setInitialPosition(Vec3 newPos) {
    }

    interface InteractionHandler {
        InteractionHandler EMPTY = new InteractionHandler() {};

        static InteractionHandler redirect(Entity redirectedEntity) {
            return new InteractionHandler() {
                @Override
                public void interact(ServerPlayer player, InteractionHand hand, Vec3 pos, boolean usingSecondaryAction) {
                    player.connection.handleInteract(new ServerboundInteractPacket(redirectedEntity.getId(), hand, pos, usingSecondaryAction));
                }

                @Override
                public void attack(ServerPlayer player) {
                    player.connection.handleAttack(new ServerboundAttackPacket(redirectedEntity.getId()));
                }

                @Override
                public void pickItem(ServerPlayer player, boolean includeData) {
                    player.connection.handlePickItemFromEntity(new ServerboundPickItemFromEntityPacket(redirectedEntity.getId(), includeData));
                }
            };
        }

        default void interact(ServerPlayer player, InteractionHand hand, Vec3 pos, boolean usingSecondaryAction) {};
        default void attack(ServerPlayer player) {};
        default void pickItem(ServerPlayer player, boolean includeData) {};
    }
}
