package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(EntitySpawnS2CPacket.class)
public class EntitySpawnS2CPacketMixin {
    @Redirect(method = "<init>(Lnet/minecraft/entity/Entity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getType()Lnet/minecraft/entity/EntityType;"))
    private static EntityType<?> polymer_replaceWithVirtual(Entity entity) {
        if (entity instanceof PolymerEntity polymerEntity) {
            return polymerEntity.getPolymerEntityType();
        } else {
            return entity.getType();
        }
    }
}
