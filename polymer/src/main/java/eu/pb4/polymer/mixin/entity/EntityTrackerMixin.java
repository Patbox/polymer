package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.impl.interfaces.MetaConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public abstract class EntityTrackerMixin {
    @Shadow @Final private Set<EntityTrackingListener> listeners;

    @Shadow @Final private Entity entity;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/EntityTrackerEntry;<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;IZLjava/util/function/Consumer;)V"))
    private Consumer<Packet<?>> polymer_replaceReceiver(Consumer<Packet<?>> receiver) {
        return MetaConsumer.plsFixInDevRemappingFabric((ThreadedAnvilChunkStorage.EntityTracker) (Object) this, this.listeners, this.entity);
    }
}
