package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.CommonClientConnectionExt;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements CommonClientConnectionExt {
    @Shadow @Nullable public abstract PacketListener getPacketListener();

    @Unique
    private boolean polymerCommon$hasResourcePack = false;

    @Override
    public boolean polymerCommon$hasResourcePack() {
        return this.polymerCommon$hasResourcePack;
    }

    @Override
    public void polymerCommon$setResourcePack(boolean value) {
        var old = this.polymerCommon$hasResourcePack;
        this.polymerCommon$hasResourcePack = value;

        if (this.getPacketListener() instanceof ServerPlayNetworkHandler handler) {
            PolymerCommonUtils.ON_RESOURCE_PACK_STATUS_CHANGE.invoke(x -> x.onResourcePackChange(handler, old, value));
        }
    }

    @Override
    public void polymerCommon$setResourcePackNoEvent(boolean value) {
        this.polymerCommon$hasResourcePack = value;
    }
}
