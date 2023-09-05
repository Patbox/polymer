package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
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

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void polymerNet$sendHandshake(GameJoinS2CPacket packet, CallbackInfo ci) {
        ClientPacketRegistry.sendHandshake((ClientPlayNetworkHandler) (Object) this);
    }
}
