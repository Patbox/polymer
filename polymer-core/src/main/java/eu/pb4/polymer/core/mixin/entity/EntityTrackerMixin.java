package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.MetaConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public abstract class EntityTrackerMixin {

    @Shadow @Final private Set<PlayerAssociatedNetworkHandler> listeners;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;IZLjava/util/function/Consumer;)Lnet/minecraft/server/network/EntityTrackerEntry;"))
    private EntityTrackerEntry polymer$replaceReceiver(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer<Packet<?>> receiver) {
        return new EntityTrackerEntry(world, entity, tickInterval, alwaysUpdateVelocity, MetaConsumer.sendToOtherPlayers((ServerChunkLoadingManager.EntityTracker) (Object) this, this.listeners, entity));
    }
}
