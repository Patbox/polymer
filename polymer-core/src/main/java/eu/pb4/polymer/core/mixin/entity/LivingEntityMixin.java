package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @ModifyArg(method = "handleEquipmentChanges(Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;sendToTrackingPlayers(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"))
    private Packet<?> polymer_addPlayerContext(Packet<?> packet) {
        return EntityAttachedPacket.setIfEmpty(packet, this);
    }

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
}
