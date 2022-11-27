package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEventS2CPacket.class)
public abstract class BlockEventS2CPacketMixin {
    @Shadow
    @Mutable
    private Block block;

    @Shadow
    public abstract Block getBlock();

    @Environment(EnvType.CLIENT)
    @Inject(method = "getBlock", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceBlockClient(CallbackInfoReturnable<Block> cir) {
        if (ClientUtils.isSingleplayer() && !(PolymerClientDecoded.checkDecode(this.block))) {
            cir.setReturnValue(PolymerBlockUtils.getPolymerBlock(block, ClientUtils.getPlayer()));
        }
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;)V"))
    private Object polymer$replaceBlockLocal(Object block) {
        return PolymerBlockUtils.getPolymerBlock((Block) block, PolymerUtils.getPlayer());
    }
}
