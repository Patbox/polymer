package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonClientConnectionExt;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonNetworkHandlerExt;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements CommonNetworkHandlerExt {
    @Shadow @Final protected ClientConnection connection;
    @Unique
    private boolean polymerCommon$ignoreNextStatus = false;

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void polymer$changeStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (!CommonImplUtils.disableResourcePackCheck && packet.getStatus() != ResourcePackStatusC2SPacket.Status.ACCEPTED) {
            if (!this.polymerCommon$ignoreNextStatus) {
                ((CommonClientConnectionExt) this.connection).polymerCommon$setResourcePack(switch (packet.getStatus()) {
                    case SUCCESSFULLY_LOADED -> true;
                    case DECLINED, FAILED_DOWNLOAD, ACCEPTED -> false;
                });
            }

            this.polymerCommon$ignoreNextStatus = false;
        }
    }

    @Override
    public void polymerCommon$setIgnoreNextResourcePack() {
        this.polymerCommon$ignoreNextStatus = true;
    }

    @Override
    public ClientConnection polymerCommon$getConnection() {
        return this.connection;
    }
}
