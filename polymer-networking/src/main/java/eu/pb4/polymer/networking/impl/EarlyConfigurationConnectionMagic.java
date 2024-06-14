package eu.pb4.polymer.networking.impl;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.api.server.EarlyConfigurationNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public class EarlyConfigurationConnectionMagic {
    private static final List<Function<EarlyConfigurationNetworkHandler.Context, EarlyConfigurationNetworkHandler>> CONSTRUCTORS = new ArrayList<>();

    public static void handle(GameProfile profile, SyncedClientOptions options, ServerLoginNetworkHandler loginHandler, MinecraftServer server, ClientConnection connection, Consumer<ContextImpl> finish) {
        var iterator = new ArrayList<>(CONSTRUCTORS).iterator();

        var ctx = new ContextImpl(server, profile, connection, loginHandler, new ArrayList<>(), (c) -> {
            while (iterator.hasNext()) {
                var handler = iterator.next().apply(c);
                if (handler != null) {
                    return;
                }
            }
            finish.accept(c);
        }, new AtomicReference<>(options));


        connection.transitionInbound(ConfigurationStates.C2S,
                new FallbackServerPacketHandler(NetworkPhase.CONFIGURATION, ctx.options()::set, ctx.storedPackets()::add, loginHandler::onDisconnected));

        ctx.continueRunning().accept(ctx);
    }

    public static void register(Function<EarlyConfigurationNetworkHandler.Context, @Nullable EarlyConfigurationNetworkHandler> constructor) {
        CONSTRUCTORS.add(constructor);
    }

    static {
        register(PolymerHandshakeHandlerImplLogin::create);
    }

    public record ContextImpl(
            MinecraftServer server,
            GameProfile profile,
            ClientConnection connection,
            ServerLoginNetworkHandler loginHandler,
            List<Packet<?>> storedPackets,
            Consumer<ContextImpl> continueRunning,
            AtomicReference<SyncedClientOptions> options
    ) implements EarlyConfigurationNetworkHandler.Context {
    }
}
