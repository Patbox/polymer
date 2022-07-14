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

import java.util.ArrayList;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public ClientConnection connection;

    @Shadow protected abstract void addToServer(ServerPlayerEntity player);

    @Shadow private @Nullable GameProfile profile;
    @Shadow private ServerLoginNetworkHandler.State state;
    @Unique
    private boolean polymer_passPlayer = false;

    @Inject(method = "addToServer", at = @At("HEAD"), cancellable = true)
    private void polymer_prePlayHandshakeHackfest(ServerPlayerEntity player, CallbackInfo ci) {
        if (!this.polymer_passPlayer && PolymerImpl.ENABLE_NETWORKING_SERVER) {
            EarlyConnectionMagic.handle(player, server, connection, () -> {
                if (!this.polymer_passPlayer) {
                    this.polymer_passPlayer = true;
                    this.connection.setPacketListener((PacketListener) this);

                    if (this.connection.isOpen()) {
                        var oldPlayer = this.server.getPlayerManager().getPlayer(this.profile.getId());
                        if (oldPlayer != null) {
                            this.state = ServerLoginNetworkHandler.State.DELAY_ACCEPT;

                            var list = new ArrayList<ServerPlayerEntity>();

                            var pm = this.server.getPlayerManager();
                            for(int i = 0; i < pm.getPlayerList().size(); ++i) {
                                var existingPlayer = pm.getPlayerList().get(i);
                                if (existingPlayer.getUuid().equals(profile.getId())) {
                                    list.add(existingPlayer);
                                }
                            }

                            if (oldPlayer != null && !list.contains(oldPlayer)) {
                                list.add(oldPlayer);
                            }

                            for (var playerKick : list) {
                                if (playerKick.networkHandler.connection.isOpen()) {
                                    playerKick.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.duplicate_login"));
                                } else {
                                    playerKick.networkHandler.onDisconnected(Text.translatable("multiplayer.disconnect.duplicate_login"));
                                }
                            }
                        } else {
                            this.addToServer(player);
                        }
                    }
                }
            });
            ci.cancel();
        }
    }
}
