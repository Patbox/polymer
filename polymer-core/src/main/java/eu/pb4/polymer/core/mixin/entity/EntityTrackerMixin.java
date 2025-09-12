package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.entity.PolymerTrackerPacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;

@Mixin(ServerChunkLoadingManager.EntityTracker.class)
public abstract class EntityTrackerMixin {

    @Shadow
    @Final
    private Set<PlayerAssociatedNetworkHandler> listeners;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/EntityTrackerEntry;<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;IZLnet/minecraft/server/network/EntityTrackerEntry$TrackerPacketSender;)V"))
    private EntityTrackerEntry.TrackerPacketSender replaceReceiver(EntityTrackerEntry.TrackerPacketSender sender, @Local(argsOnly = true) Entity entity) {
        return PolymerTrackerPacketSender.of(sender, () -> this.listeners, entity);
    }
}
