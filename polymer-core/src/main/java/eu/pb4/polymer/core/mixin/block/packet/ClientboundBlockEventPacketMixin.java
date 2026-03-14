package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(ClientboundBlockEventPacket.class)
public abstract class ClientboundBlockEventPacketMixin {
    @Shadow
    public abstract Block getBlock();

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object polymer$replaceBlockLocal(Object block) {
        return PolymerBlockUtils.getPolymerBlock((Block) block, PacketContext.get());
    }
}
