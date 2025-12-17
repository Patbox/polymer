package eu.pb4.polymer.common.mixin.client;

import eu.pb4.polymer.common.impl.CommonPacketListenerImplExt;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin implements CommonPacketListenerImplExt {
    @Shadow @Final protected Connection connection;

    @Override
    public void polymerCommon$setIgnoreNextResourcePack() {
    }

    @Override
    public Connection polymerCommon$getConnection() {
        return this.connection;
    }
}
