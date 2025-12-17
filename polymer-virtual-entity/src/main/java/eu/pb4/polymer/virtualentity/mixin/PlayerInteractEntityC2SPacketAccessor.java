package eu.pb4.polymer.virtualentity.mixin;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundInteractPacket.class)
public interface PlayerInteractEntityC2SPacketAccessor {
    @Accessor
    int getEntityId();
}
