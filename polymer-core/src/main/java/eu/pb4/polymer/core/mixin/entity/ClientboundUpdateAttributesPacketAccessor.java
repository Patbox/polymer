package eu.pb4.polymer.core.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;

@Mixin(ClientboundUpdateAttributesPacket.class)
public interface ClientboundUpdateAttributesPacketAccessor {
    @Accessor
    List<ClientboundUpdateAttributesPacket.AttributeSnapshot> getAttributes();
}
