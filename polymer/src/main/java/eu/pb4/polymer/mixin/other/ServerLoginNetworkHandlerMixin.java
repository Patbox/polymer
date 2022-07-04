package eu.pb4.polymer.mixin.other;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.networking.EarlyConnectionMagic;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public ClientConnection connection;

    @Shadow protected abstract void addToServer(ServerPlayerEntity player);

    @Shadow private @Nullable GameProfile profile;
    @Unique
    private boolean polymer_passPlayer = false;

    @Inject(method = "addToServer", at = @At("HEAD"), cancellable = true)
    private void polymer_prePlayHandshakeHackfest(ServerPlayerEntity player, CallbackInfo ci) {
        if (!this.polymer_passPlayer && PolymerImpl.ENABLE_NETWORKING_SERVER) {
            EarlyConnectionMagic.handle(player, server, connection, () -> {
                if (!this.polymer_passPlayer) {
                    this.polymer_passPlayer = true;
                    this.connection.setPacketListener((PacketListener) this);

                    var oldPlayer = this.server.getPlayerManager().getPlayer(this.profile.getId());
                    if (oldPlayer != null) {
                        oldPlayer.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.duplicate_login"));
                        this.server.getPlayerManager().remove(oldPlayer);
                    } else {
                        this.addToServer(player);
                    }
                }
            });
            ci.cancel();
        }
    }
}
