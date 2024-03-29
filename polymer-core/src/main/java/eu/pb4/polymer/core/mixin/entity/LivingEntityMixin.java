package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @ModifyArg(method = "sendEquipmentChanges(Ljava/util/Map;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;sendToOtherNearbyPlayers(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/packet/Packet;)V"))
    private Packet<?> polymer_addPlayerContext(Packet<?> packet) {
        return EntityAttachedPacket.setIfEmpty(packet, this);
    }

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
}
