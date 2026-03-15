package eu.pb4.polymer.core.api.entity;

import eu.pb4.polymer.core.impl.entity.DirectServerEntitySynchronizer;
import eu.pb4.polymer.core.impl.entity.PolymericServerEntitySynchronizer;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public interface PolymerServerEntitySynchronizer extends ServerEntity.Synchronizer {
    Set<ServerPlayerConnection> listeners();

    static PolymerServerEntitySynchronizer of(ServerEntity.Synchronizer tracker, Supplier<Set<ServerPlayerConnection>> listeners, Entity entity) {
        if (PolymerEntity.get(entity) instanceof PolymerEntity polymerEntity) {
            return new PolymericServerEntitySynchronizer(tracker, listeners, entity, polymerEntity);
        } else {
            return new DirectServerEntitySynchronizer(tracker, listeners, entity);
        }
    }
}
