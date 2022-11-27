package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocol;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocolHandler;
import eu.pb4.polymer.core.impl.networking.PolymerHandshakeHandlerImplLogin;
import eu.pb4.polymer.core.mixin.other.CustomPayloadS2CPacketAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow public abstract ClientWorld getWorld();

    @Shadow @Final private MinecraftClient client;

    @Shadow private ClientWorld world;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void polymer_sendHandshake(GameJoinS2CPacket packet, CallbackInfo ci) {
        PolymerClientProtocol.sendHandshake((ClientPlayNetworkHandler) (Object) this);
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymer$catchPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.getChannel().getNamespace().equals(PolymerUtils.ID)) {
            var buf = ((CustomPayloadS2CPacketAccessor) packet).polymer$getData();
            PolymerClientProtocolHandler.handle((ClientPlayNetworkHandler) (Object) this, packet.getChannel(), buf);
            buf.release();
            ci.cancel();
        }
    }

    @Inject(method = "onKeepAlive", at = @At("HEAD"))
    private void polymer$handleHackfest(KeepAliveS2CPacket packet, CallbackInfo ci) {
        // Yes, it's a hack but it works quite well!
        // I should replace it with some api later
        if (packet.getId() == PolymerHandshakeHandlerImplLogin.MAGIC_VALUE) {
            PolymerClientProtocol.sendHandshake((ClientPlayNetworkHandler) (Object) this);
        }
    }

    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void polymer$removeOldBlock(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        // This should be overriden by next polymer packet anyway
        // Thanks to it there is no need to send vanilla updates!
        InternalClientRegistry.setBlockAt(packet.getPos(), ClientPolymerBlock.NONE_STATE);
    }

    @Inject(method = "method_34007", at = @At("TAIL"))
    private void polymer$removeOldBlock2(int i, BlockPos pos, BlockState state, CallbackInfo ci) {
        InternalClientRegistry.setBlockAt(pos, ClientPolymerBlock.NONE_STATE);
    }
}
