package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@Mixin(ServerEntity.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity entity;

    @Shadow private List<Entity> lastPassengers;


    @Shadow @Final private ServerEntity.Synchronizer synchronizer;

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void polymerVE$startTracking(ServerPlayer player, CallbackInfo ci) {
        boolean hasPassangers = false;
        var a = ((HolderAttachmentHolder) this.entity).polymerVE$getHolders();
        if (!a.isEmpty()) {
            for (var x : a) {
                x.startWatching(player);
                hasPassangers |= !x.holder().getAttachedPassengerEntityIds().isEmpty();
            }
        }

        if (hasPassangers || !((EntityExt) this.entity).polymerVE$getVirtualRidden().isEmpty()) {
            player.connection.send(new ClientboundSetPassengersPacket(this.entity));
        }
    }

    @Inject(method = "sendChanges", at = @At("HEAD"))
    private void polymerVE$tick(CallbackInfo ci) {
        var a = ((HolderAttachmentHolder) this.entity).polymerVE$getHolders();
        if (!a.isEmpty()) {
            var arr = a.toArray(HolderHolder.HOLDER_ATTACHMENTS);
            for (int i = 0; i < arr.length; i++) {
                arr[i].tick();
            }
        }

        if (((EntityExt) this.entity).polymerVE$getAndClearVirtualRiddenDirty() && this.entity.getPassengers().equals(this.lastPassengers)) {
            this.synchronizer.sendToTrackingPlayersAndSelf(new ClientboundSetPassengersPacket(this.entity));
        }
    }

    @Inject(method = "removePairing", at = @At("TAIL"))
    private void polymerVE$stopTracking(ServerPlayer player, CallbackInfo ci) {
        for (var x : ((HolderAttachmentHolder) this.entity).polymerVE$getHolders()) {
            x.stopWatching(player);
        }
    }
}
