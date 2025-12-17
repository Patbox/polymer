package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({
        ClientboundMoveEntityPacket.class,
        ClientboundAddEntityPacket.class,
        ClientboundSetEntityDataPacket.class,
        ClientboundAnimatePacket.class,
        ClientboundTeleportEntityPacket.class,
        ClientboundRotateHeadPacket.class,
        ClientboundSetEquipmentPacket.class,
        ClientboundUpdateAttributesPacket.class
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
