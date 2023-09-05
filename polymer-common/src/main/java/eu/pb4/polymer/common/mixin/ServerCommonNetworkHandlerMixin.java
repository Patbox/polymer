package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import net.minecraft.class_8792;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements CommonResourcePackInfoHolder {
    @Unique
    private boolean polymerCommon$hasResourcePack = false;
    @Unique
    private boolean polymerCommon$ignoreNextStatus = false;

    /*@Inject(method = "<init>", at = @At("TAIL"))
    private void polymerCommon$setRP(MinecraftServer server, ClientConnection connection, class_8792 arg, CallbackInfo ci) {
        this.polymerCommon$hasResourcePack = ((CommonResourcePackInfoHolder) player).polymerCommon$hasResourcePack();
    }*/

    @Override
    public boolean polymerCommon$hasResourcePack() {
        return this.polymerCommon$hasResourcePack;
    }

    @Override
    public void polymerCommon$setResourcePack(boolean value) {
        var old = this.polymerCommon$hasResourcePack;
        this.polymerCommon$hasResourcePack = value;

        PolymerCommonUtils.ON_RESOURCE_PACK_STATUS_CHANGE.invoke(x -> x.onResourcePackChange((ServerPlayNetworkHandler) (Object) this, old, value));
    }

    @Override
    public void polymerCommon$setResourcePackNoEvent(boolean value) {
        this.polymerCommon$hasResourcePack = value;
    }

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void polymer$changeStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (!CommonImplUtils.disableResourcePackCheck && packet.getStatus() != ResourcePackStatusC2SPacket.Status.ACCEPTED) {
            if (this.polymerCommon$ignoreNextStatus == false) {
                this.polymerCommon$setResourcePack(switch (packet.getStatus()) {
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
}
