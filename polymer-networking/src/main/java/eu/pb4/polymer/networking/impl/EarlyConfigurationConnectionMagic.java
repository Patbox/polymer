package eu.pb4.polymer.networking.impl;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.api.server.EarlyConfigurationNetworkHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
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

    public static void handle(GameProfile profile, ClientInformation options, ServerLoginPacketListenerImpl loginHandler, MinecraftServer server, Connection connection, Consumer<ContextImpl> finish) {
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


        connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND,
                new FallbackServerPacketHandler(ConnectionProtocol.CONFIGURATION, ctx.options()::set, ctx.storedPackets()::add, loginHandler::onDisconnect));

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
            Connection connection,
            ServerLoginPacketListenerImpl loginHandler,
            List<Packet<?>> storedPackets,
            Consumer<ContextImpl> continueRunning,
            AtomicReference<ClientInformation> options
    ) implements EarlyConfigurationNetworkHandler.Context {
    }
}
