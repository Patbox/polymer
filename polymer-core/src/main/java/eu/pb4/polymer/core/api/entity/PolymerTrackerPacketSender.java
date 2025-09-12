package eu.pb4.polymer.core.api.entity;

import eu.pb4.polymer.core.impl.entity.DirectTrackerPacketSender;
import eu.pb4.polymer.core.impl.entity.PolymericTrackerPacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;

import java.util.Set;
import java.util.function.Supplier;

public interface PolymerTrackerPacketSender extends EntityTrackerEntry.TrackerPacketSender {
    Set<PlayerAssociatedNetworkHandler> listeners();

    static PolymerTrackerPacketSender of(EntityTrackerEntry.TrackerPacketSender tracker, Supplier<Set<PlayerAssociatedNetworkHandler>> listeners, Entity entity) {
        if (PolymerEntity.get(entity) instanceof PolymerEntity polymerEntity) {
            return new PolymericTrackerPacketSender(tracker, listeners, entity, polymerEntity);
        } else {
            return new DirectTrackerPacketSender(tracker, listeners, entity);
        }
    }
}
