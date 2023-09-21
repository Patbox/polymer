package eu.pb4.polymer.networking.impl;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.FakeRegistry;
import eu.pb4.polymer.networking.api.server.EarlyConfigurationNetworkHandler;
import eu.pb4.polymer.networking.api.server.EarlyPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public class EarlyConfigurationConnectionMagic {
    private static final List<Function<EarlyConfigurationNetworkHandler.Context, EarlyConfigurationNetworkHandler>> CONSTRUCTORS = new ArrayList<>();

    public static void handle(GameProfile profile, SyncedClientOptions options, ServerLoginNetworkHandler loginHandler, MinecraftServer server, ClientConnection connection, Consumer<ContextImpl> finish) {
        var iterator = new ArrayList<>(CONSTRUCTORS).iterator();

        var context = new ContextImpl(server, profile, connection, loginHandler, new ArrayList<>(), (c) -> {
            connection.disableAutoRead();
            while (iterator.hasNext()) {
                var handler = iterator.next().apply(c);
                if (handler != null) {
                    connection.enableAutoRead();
                    return;
                }
            }
            finish.accept(c);
        }, new MutableObject<>(options));

        context.continueRunning.accept(context);
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
            List<CustomPayloadC2SPacket> storedPackets,
            Consumer<ContextImpl> continueRunning,
            MutableObject<SyncedClientOptions> options
    ) implements EarlyConfigurationNetworkHandler.Context {
    }
}
