package eu.pb4.polymer.core.mixin.block.packet;

import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundBlockUpdatePacket.class)
public interface ClientboundBlockUpdatePacketAccessor {
    @Accessor("blockState")
    BlockState polymer$getState();
}
