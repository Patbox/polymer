package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.impl.CommonConnectionExt;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonPacketListenerImplExt;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 1100)
public abstract class ServerCommonPacketListenerImplMixin implements CommonPacketListenerImplExt {
    @Shadow @Final protected Connection connection;
    @Unique
    private boolean polymerCommon$ignoreNextStatus = false;

    @Inject(method = "handleResourcePackResponse", at = @At("HEAD"))
    private void polymer$changeStatus(ServerboundResourcePackPacket packet, CallbackInfo ci) {
        if (!CommonImplUtils.disableResourcePackCheck) {
            if (!this.polymerCommon$ignoreNextStatus) {
                ((CommonConnectionExt) this.connection).polymerCommon$setResourcePack(packet.id(), switch (packet.action()) {
                    case SUCCESSFULLY_LOADED, DOWNLOADED, ACCEPTED -> true;
                    case DECLINED, FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD, DISCARDED -> false;
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
    public Connection polymerCommon$getConnection() {
        return this.connection;
    }
}
