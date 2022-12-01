package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.networking.api.EarlyPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public class EarlyConnectionMagic {
    private static final List<Function<EarlyPlayNetworkHandler.Context, EarlyPlayNetworkHandler>> CONSTRUCTORS = new ArrayList<>();

    public static void handle(ServerPlayerEntity player, MinecraftServer server, ClientConnection connection, Consumer<ContextImpl> finish) {
        var iterator = new ArrayList<>(CONSTRUCTORS).iterator();

        if (iterator.hasNext()) {
            var context = new ContextImpl(server, player, connection, new ArrayList<>(), (c) -> {

                if (iterator.hasNext()) {
                    iterator.next().apply(c);
                } else {
                    finish.accept(c);
                }
            });

            ((TempPlayerLoginAttachments) player).polymer$setLatePackets(context.storedPackets);

            iterator.next().apply(context);
        }
    }

    public static void register(Function<EarlyPlayNetworkHandler.Context, EarlyPlayNetworkHandler> constructor) {
        CONSTRUCTORS.add(constructor);
    }

    static {
        EarlyConnectionMagic.register(PolymerHandshakeHandlerImplLogin::new);
    }

    public record ContextImpl(
            MinecraftServer server,
            ServerPlayerEntity player,
            ClientConnection connection,
            List<CustomPayloadC2SPacket> storedPackets,
            Consumer<ContextImpl> continueRunning
    ) implements EarlyPlayNetworkHandler.Context {}
}
