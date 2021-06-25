package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(BlockEventS2CPacket.class)
public class BlockEventS2CPacketMixin {
    @Shadow @Mutable
    private Block block;

    @Shadow @Final private BlockPos pos;

    @Unique private Block oldBlock = null;

    @Environment(EnvType.CLIENT)
    @Inject(method = "getBlock", at = @At("TAIL"), cancellable = true)
    private void replaceBlockClient(CallbackInfoReturnable<Block> cir) {
        if (MinecraftClient.getInstance().getServer() != null) {
            ServerPlayerEntity player = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid());
            if (this.oldBlock instanceof VirtualBlock virtualBlock) {
                cir.setReturnValue(virtualBlock.getVirtualBlock(this.pos, player.getServerWorld()));
            }
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void replaceBlock(PacketByteBuf byteBuf, CallbackInfo ci) {
        if (oldBlock == null) {
            this.oldBlock = block;
        }

        if (this.oldBlock instanceof VirtualBlock virtualBlock) {
            this.block = virtualBlock.getVirtualBlock(this.pos, PacketContext.get().getTarget().getServerWorld());
        }
    }
}
