package eu.pb4.polymer.common.mixin.client;

import eu.pb4.polymer.common.impl.CommonNetworkHandlerExt;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin implements CommonNetworkHandlerExt {
    @Shadow @Final protected ClientConnection connection;

    @Override
    public void polymerCommon$setIgnoreNextResourcePack() {
    }

    @Override
    public ClientConnection polymerCommon$getConnection() {
        return this.connection;
    }
}
