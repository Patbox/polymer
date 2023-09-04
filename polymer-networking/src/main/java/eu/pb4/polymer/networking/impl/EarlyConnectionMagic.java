package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.networking.api.EarlyPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public class EarlyConnectionMagic {
    private static final List<Function<EarlyPlayNetworkHandler.Context, EarlyPlayNetworkHandler>> CONSTRUCTORS = new ArrayList<>();

    public static void handle(ServerPlayerEntity player, ServerLoginNetworkHandler loginHandler, MinecraftServer server, ClientConnection connection, Consumer<ContextImpl> finish) {
        var iterator = new ArrayList<>(CONSTRUCTORS).iterator();

        var context = new ContextImpl(server, player, connection, loginHandler, new ArrayList<>(), (c) -> {
            while (iterator.hasNext()) {
                var handler = iterator.next().apply(c);
                if (handler != null) {
                    return;
                }
            }

            finish.accept(c);
        });

        ((TempPlayerLoginAttachments) player).polymerNet$setLatePackets(context.storedPackets);

        context.continueRunning.accept(context);
    }

    public static void register(Function<EarlyPlayNetworkHandler.Context, @Nullable EarlyPlayNetworkHandler> constructor) {
        CONSTRUCTORS.add(constructor);
    }

    static {
        EarlyConnectionMagic.register(PolymerHandshakeHandlerImplLogin::create);
    }

    public record ContextImpl(
            MinecraftServer server,
            ServerPlayerEntity player,
            ClientConnection connection,
            ServerLoginNetworkHandler loginHandler,
            List<CustomPayloadC2SPacket> storedPackets,
            Consumer<ContextImpl> continueRunning
    ) implements EarlyPlayNetworkHandler.Context {
    }
}
