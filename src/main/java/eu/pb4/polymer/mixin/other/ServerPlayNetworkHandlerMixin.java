package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.other.PlayerRP;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements PlayerRP {
    @Unique
    private boolean polymer_hasResourcePack = false;

    @Override
    public boolean polymer_hasResourcePack() {
        return this.polymer_hasResourcePack;
    }

    @Override
    public void polymer_setResourcePack(boolean value) {
        this.polymer_hasResourcePack = value;
    }

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void polymer_changeStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (ResourcePackUtils.shouldCheckByDefault()) {
            this.polymer_hasResourcePack = switch (packet.getStatus()) {
                case ACCEPTED, SUCCESSFULLY_LOADED -> true;
                case DECLINED, FAILED_DOWNLOAD -> false;
            };
        }
    }
}
