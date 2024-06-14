package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.networking.api.server.EarlyPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public class EarlyPlayConnectionMagic {
    private static final List<Function<EarlyPlayNetworkHandler.Context, EarlyPlayNetworkHandler>> CONSTRUCTORS = new ArrayList<>();

    public static void handle(ServerPlayerEntity player, SyncedClientOptions options, ServerConfigurationNetworkHandler loginHandler, MinecraftServer server, ClientConnection connection, Consumer<ContextImpl> finish) {
        var iterator = new ArrayList<>(CONSTRUCTORS).iterator();

        var context = new ContextImpl(server, player, connection, loginHandler, new ArrayList<>(), (c) -> {
            //connection.disableAutoRead();
            while (iterator.hasNext()) {
                var handler = iterator.next().apply(c);
                if (handler != null) {
                    //connection.enableAutoRead();
                    return;
                }
            }
            finish.accept(c);
        }, new MutableObject<>(options));

        context.continueRunning.accept(context);
    }

    public static void register(Function<EarlyPlayNetworkHandler.Context, @Nullable EarlyPlayNetworkHandler> constructor) {
        CONSTRUCTORS.add(constructor);
    }

    public record ContextImpl(
            MinecraftServer server,
            ServerPlayerEntity player,
            ClientConnection connection,
            ServerConfigurationNetworkHandler loginHandler,
            List<Packet<?>> storedPackets,
            Consumer<ContextImpl> continueRunning,
            MutableObject<SyncedClientOptions> options
    ) implements EarlyPlayNetworkHandler.Context {
        public void addStoredPacket(Packet<?> packet) {
            if (packet instanceof CustomPayloadC2SPacket || packet instanceof ClientOptionsC2SPacket) {
                this.storedPackets.add(packet);
            }
        }
    }
}
