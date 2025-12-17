package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.entity.PolymerTrackerPacketSender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

@Mixin(ChunkMap.TrackedEntity.class)
public abstract class TrackedEntityMixin {

    @Shadow
    @Final
    private Set<ServerPlayerConnection> seenBy;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;IZLnet/minecraft/server/level/ServerEntity$Synchronizer;)V"))
    private ServerEntity.Synchronizer replaceReceiver(ServerEntity.Synchronizer sender, @Local(argsOnly = true) Entity entity) {
        return PolymerTrackerPacketSender.of(sender, () -> this.seenBy, entity);
    }
}
