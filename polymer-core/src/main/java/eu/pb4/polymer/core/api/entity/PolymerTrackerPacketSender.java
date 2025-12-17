package eu.pb4.polymer.core.api.entity;

import eu.pb4.polymer.core.impl.entity.DirectTrackerPacketSender;
import eu.pb4.polymer.core.impl.entity.PolymericTrackerPacketSender;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public interface PolymerTrackerPacketSender extends ServerEntity.Synchronizer {
    Set<ServerPlayerConnection> listeners();

    static PolymerTrackerPacketSender of(ServerEntity.Synchronizer tracker, Supplier<Set<ServerPlayerConnection>> listeners, Entity entity) {
        if (PolymerEntity.get(entity) instanceof PolymerEntity polymerEntity) {
            return new PolymericTrackerPacketSender(tracker, listeners, entity, polymerEntity);
        } else {
            return new DirectTrackerPacketSender(tracker, listeners, entity);
        }
    }
}
