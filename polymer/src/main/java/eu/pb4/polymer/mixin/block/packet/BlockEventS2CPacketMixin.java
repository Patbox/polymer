package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
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
    private void polymer_replaceBlockClient(CallbackInfoReturnable<Block> cir) {
        if (ClientUtils.isSingleplayer() && !(PolymerClientDecoded.checkDecode(this.block))) {
            cir.setReturnValue(PolymerBlockUtils.getPolymerBlock(block, ClientUtils.getPlayer()));
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(DefaultedRegistry instance, int index) {
        var object = InternalClientRegistry.BLOCKS.get(index);

        if (object.registryEntry() != null) {
            return object.registryEntry();
        }

        return instance.get(index);
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private Object polymer_replaceBlockLocal(Object block) {
        return PolymerBlockUtils.getPolymerBlock((Block) block, PolymerUtils.getPlayer());
    }
}
