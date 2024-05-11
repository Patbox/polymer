package eu.pb4.polymer.core.mixin.entity;

import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EntityAttributesS2CPacket.class)
public interface EntityAttributesS2CPacketAccessor {
    @Accessor
    List<EntityAttributesS2CPacket.Entry> getEntries();
}
