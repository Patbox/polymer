package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEventS2CPacket.class)
public class BlockEventS2CPacketMixin {
    @Shadow @Mutable
    private Block block;

    @Shadow @Final private BlockPos pos;

    @Unique private Block polymer_oldBlock = null;

    @Environment(EnvType.CLIENT)
    @Inject(method = "getBlock", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceBlockClient(CallbackInfoReturnable<Block> cir) {
        if (ClientUtils.isSingleplayer() && this.polymer_oldBlock == null && this.block instanceof PolymerBlock virtualBlock) {
            this.polymer_oldBlock = this.block;
            this.block = PolymerBlockUtils.getBlockSafely(virtualBlock, this.polymer_oldBlock.getDefaultState());
        }
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void polymer_replaceBlock(PacketByteBuf byteBuf, CallbackInfo ci) {
        if (polymer_oldBlock == null && this.block instanceof PolymerBlock virtualBlock) {
            this.polymer_oldBlock = block;
            this.block = PolymerBlockUtils.getBlockSafely(virtualBlock, this.polymer_oldBlock.getDefaultState());
        }
    }
}
