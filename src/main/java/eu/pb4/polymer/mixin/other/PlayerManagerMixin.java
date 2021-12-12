package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.impl.interfaces.TempPlayerLoginAttachments;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @ModifyVariable(method = "onPlayerConnect", at = @At(value = "STORE"), require = 0)
    private ServerPlayNetworkHandler polymer_setupHandler(ServerPlayNetworkHandler handler, ClientConnection connection, ServerPlayerEntity player) {
        var handshake = ((TempPlayerLoginAttachments) player).polymer_getAndRemoveHandshakeHandler();

        if (handshake != null) {
            handshake.apply(handler);
            PolymerServerProtocol.sendSyncPackets(handler);
        }

        return handler;
    }
}
