package eu.pb4.polymer.networking.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.networking.impl.NetworkHandlerExtension;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.ContextProvidingPacketListener;
import xyz.nucleoid.packettweaker.PacketContext;

public class ContextByteBuf extends RegistryByteBuf {
    private final PacketContext context;
    private final int version;

    public static ContextByteBuf of(PacketContext context, int version, ByteBuf buf) {
        DynamicRegistryManager registryManager = null;

        if (buf instanceof RegistryByteBuf reg) {
            registryManager = reg.getRegistryManager();
        } else if (context.getPacketListener() instanceof NetworkHandlerExtension ext) {
            registryManager = ext.polymer$getDynamicRegistryManager();
        }
        if (registryManager == null) {
            registryManager = DynamicRegistryManager.EMPTY;
        }

        return new ContextByteBuf(context, version, buf, registryManager);
    }

    public ContextByteBuf(PacketContext context, int version, ByteBuf buf, DynamicRegistryManager registryManager) {
        super(buf, registryManager);
        this.context = context;
        this.version = version;
    }

    public static <T extends CustomPayload> PacketCodec<ByteBuf, T> simple(PacketCodec<ContextByteBuf, T> codec) {
        return PacketCodec.ofStatic(
                (x, y) -> codec.encode(of(PacketContext.get(), 0, x), y),
                (x) -> codec.decode(of(PacketContext.get(), 0, x)));
    }

    public static <T extends CustomPayload> PacketCodec<ByteBuf, T> versioned(Identifier identifier, PacketCodec<ContextByteBuf, T> codec) {
        return PacketCodec.ofStatic(
                (x, y) -> {
                    try {
                        var ctx = PacketContext.get();
                        var version = PolymerNetworking.getSupportedVersion(ctx.getClientConnection(), identifier);
                        VarInts.write(x, version);
                        codec.encode(of(ctx, version, x), y);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                },
                (x) -> codec.decode(of(PacketContext.get(), VarInts.read(x), x)));
    }

    public int version() {
        return this.version;
    }

    @Nullable
    public ServerPlayerEntity player() {
        return context.getPlayer();
    }
    @Nullable
    public SyncedClientOptions clientOptions() {
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
    public ClientConnection clientConnection() {
        return context.getClientConnection();
    }

    @Nullable
    public Packet<?> encodedPacket() {
        return context.getEncodedPacket();
    }
}
