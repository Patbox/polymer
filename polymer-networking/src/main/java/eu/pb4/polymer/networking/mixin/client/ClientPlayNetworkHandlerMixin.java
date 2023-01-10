package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.PolymerHandshakeHandlerImplLogin;
import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    public abstract ClientWorld getWorld();


    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void polymerNet$sendHandshake(GameJoinS2CPacket packet, CallbackInfo ci) {
        ClientPacketRegistry.sendHandshake((ClientPlayNetworkHandler) (Object) this);
    }

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (this.client.isOnThread()) {
            return;
        }

        if (ClientPacketRegistry.handle((ClientPlayNetworkHandler) (Object) this, packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "onKeepAlive", at = @At("HEAD"))
    private void polymerNet$handleHackfest(KeepAliveS2CPacket packet, CallbackInfo ci) {
        // Yes, it's a hack but it works quite well!
        // I should replace it with some api later
        if (packet.getId() == PolymerHandshakeHandlerImplLogin.MAGIC_INIT_VALUE) {
            ClientPacketRegistry.sendHandshake((ClientPlayNetworkHandler) (Object) this);
        }
    }
}
