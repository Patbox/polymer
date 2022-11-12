package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
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
    private Entity polymer_entity = null;

    @Override
    public Entity polymer_getEntity() {
        return this.polymer_entity;
    }

    @Override
    public Packet<?> polymer_setEntity(Entity entity) {
        this.polymer_entity = entity;
        return (Packet<?>) this;
    }
}
