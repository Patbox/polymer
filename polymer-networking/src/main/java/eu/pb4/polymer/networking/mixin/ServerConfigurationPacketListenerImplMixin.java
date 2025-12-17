package eu.pb4.polymer.networking.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ServerConfigurationPacketListenerImpl.class, priority = 900)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }
    @Shadow
    protected abstract GameProfile playerProfile();
}
