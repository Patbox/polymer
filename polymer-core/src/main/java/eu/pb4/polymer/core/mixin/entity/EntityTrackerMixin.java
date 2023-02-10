package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.MetaConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public abstract class EntityTrackerMixin {

    @Shadow @Final private Set<EntityTrackingListener> listeners;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/network/EntityTrackerEntry"))
    private EntityTrackerEntry polymer$replaceReceiver(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer<Packet<?>> receiver) {
        return new EntityTrackerEntry(world, entity, tickInterval, alwaysUpdateVelocity, MetaConsumer.sendToOtherPlayers((ThreadedAnvilChunkStorage.EntityTracker) (Object) this, this.listeners, entity));
    }
}
