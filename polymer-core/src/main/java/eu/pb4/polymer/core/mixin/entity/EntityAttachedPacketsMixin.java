package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({
        EntityS2CPacket.class,
        EntitySpawnS2CPacket.class,
        PlayerSpawnS2CPacket.class,
        EntityTrackerUpdateS2CPacket.class,
        EntityPositionS2CPacket.class,
        EntitySetHeadYawS2CPacket.class,
        EntityEquipmentUpdateS2CPacket.class,
        EntityAttributesS2CPacket.class
})
public class EntityAttachedPacketsMixin implements EntityAttachedPacket {
    @Unique
    private Entity polymer$entity = null;

    @Override
    public Entity polymer$getEntity() {
        return this.polymer$entity;
    }

    @Override
    public Packet<?> polymer$setEntity(Entity entity) {
        this.polymer$entity = entity;
        return (Packet<?>) this;
    }
}
