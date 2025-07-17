package eu.pb4.polymer.virtualentity.mixin.accessors;

import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAttachS2CPacket.class)
public interface EntityAttachS2CPacketAccessor {

    @Mutable
    @Accessor
    void setAttachedEntityId(int attachedEntityId);

    @Mutable
    @Accessor
    void setHoldingEntityId(int holdingEntityId);
}
