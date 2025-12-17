package eu.pb4.polymer.networking.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.ContextProvidingPacketListener;
import xyz.nucleoid.packettweaker.PacketContext;

public class ContextByteBuf extends RegistryFriendlyByteBuf {
    private final PacketContext context;
    private final int version;

    public static ContextByteBuf of(PacketContext context, int version, ByteBuf buf) {
        RegistryAccess registryManager = null;

        if (buf instanceof RegistryFriendlyByteBuf reg) {
            registryManager = reg.registryAccess();
        } else if (context.getBackingPacketListener() instanceof PacketListenerImplExtension ext) {
            registryManager = ext.polymer$getDynamicRegistryManager();
        }
        if (registryManager == null) {
            registryManager = RegistryAccess.EMPTY;
        }

        return new ContextByteBuf(context, version, buf, registryManager);
    }

    public ContextByteBuf(PacketContext context, int version, ByteBuf buf, RegistryAccess registryManager) {
        super(buf, registryManager);
        this.context = context;
        this.version = version;
    }

    public static <T extends CustomPacketPayload> StreamCodec<ByteBuf, T> simple(StreamCodec<ContextByteBuf, T> codec) {
        return StreamCodec.of(
                (x, y) -> codec.encode(of(PacketContext.get(), 0, x), y),
                (x) -> codec.decode(of(PacketContext.get(), 0, x)));
    }

    public static <T extends CustomPacketPayload> StreamCodec<ByteBuf, T> versioned(Identifier identifier, StreamCodec<ContextByteBuf, T> codec) {
        return StreamCodec.of(
                (x, y) -> {
                    try {
                        var ctx = PacketContext.get();
                        var version = PolymerNetworking.getSupportedVersion(ctx.getClientConnection(), identifier);
                        VarInt.write(x, version);
                        codec.encode(of(ctx, version, x), y);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                },
                (x) -> codec.decode(of(PacketContext.get(), VarInt.read(x), x)));
    }

    public int version() {
        return this.version;
    }

    @Nullable
    public ServerPlayer player() {
        return context.getPlayer();
    }
    @Nullable
    public ClientInformation clientOptions() {
        return context.getClientOptions();
    }
    @Nullable
    public GameProfile gameProfile() {
        return context.getGameProfile();
    }


    public ContextProvidingPacketListener packetListener() {
        return context.getPacketListener();
    }

    @Nullable
    public Connection clientConnection() {
        return context.getClientConnection();
    }

    @Nullable
    public Packet<?> encodedPacket() {
        return context.getEncodedPacket();
    }
}
