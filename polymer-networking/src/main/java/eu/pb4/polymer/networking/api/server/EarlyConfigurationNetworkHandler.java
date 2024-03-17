package eu.pb4.polymer.networking.api.server;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.impl.EarlyConfigurationConnectionMagic;
import eu.pb4.polymer.networking.impl.EarlyPlayConnectionMagic;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.*;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.CommonPlayerSpawnInfo;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.ContextProvidingPacketListener;

import java.util.Optional;
import java.util.function.Function;

/**
 * This api exposes Polymer's early play packets utilities.
 *
 * Use carefully, as client might not be initialized, or it might have leftover state from previous EarlyPlay handlers
 * Use this only if you know what you are doing and you need to do sync/packets before player joins a world.
 */

public class EarlyConfigurationNetworkHandler implements ServerConfigurationPacketListener, TickablePacketListener, ContextProvidingPacketListener {

    public static void register(Function<Context, EarlyConfigurationNetworkHandler> constructor) {
        EarlyConfigurationConnectionMagic.register(constructor);
    }

    private final EarlyConfigurationConnectionMagic.ContextImpl context;
    private final Identifier identifier;

    private volatile long lastResponse = 0;

    private volatile int keepAliveSent = 0;
    private volatile int keepAliveReceived = 0;
    private volatile int pingsId = 1024;
    private volatile boolean canContinue = true;
    private volatile boolean alreadyContinued;

    public EarlyConfigurationNetworkHandler(Identifier identifier, Context context) {
        this.context = (EarlyConfigurationConnectionMagic.ContextImpl) context;
        this.identifier = identifier;

        this.context.connection().transitionInbound(ConfigurationStates.C2S, this);

        this.sendKeepAlive();
    }

    public final Identifier getId() {
        return this.identifier;
    }

    public void handleDisconnect(Text reason) {

    }

    public void handleKeepAlive(long time) {

    }

    public boolean handleCustomPayload(CustomPayloadC2SPacket packet) {
        return false;
    }

    @Override
    public final void tick() {
        if (this.lastResponse == 1200) {
            this.disconnect(Text.translatable("multiplayer.disconnect.slow_login"));
        } else if (this.lastResponse == 20) {
            this.sendKeepAlive();
        }
        this.lastResponse++;
        this.onTick();
    }

    protected void onTick() {
    }

    @Override
    public final void onKeepAlive(KeepAliveC2SPacket packet) {
        this.lastResponse = -20;
        this.keepAliveReceived++;
        if (this.canContinue) {
            this.handleKeepAlive(packet.getId());
        } else if (!this.alreadyContinued && this.keepAliveReceived >= this.keepAliveSent) {
            this.alreadyContinued = true;
            this.context.server().execute(() -> this.context.continueRunning().accept(this.context));
        }
    }

    @Override
    public void onPong(CommonPongC2SPacket packet) {

    }

    public final void sendPacket(Packet<?> packet) {
        this.context.connection().send(packet);
    }

    protected void sendPacket(CustomPayload payload) {
        this.sendPacket(new CustomPayloadS2CPacket(payload));
    }

    public final void sendKeepAlive(long value) {
        this.keepAliveSent++;
        this.sendPacket(new KeepAliveS2CPacket(value));
    }

    public final void sendKeepAlive() {
        this.sendKeepAlive(System.currentTimeMillis());
    }

    public final void sendPing(int id) {
        this.sendPacket(new CommonPingS2CPacket(id));
    }

    public final int sendPing() {
        var id = this.pingsId++;
        this.sendPing(id);
        return id;
    }

    @Override
    public final void onCustomPayload(CustomPayloadC2SPacket packet) {
        if (!handleCustomPayload(packet)) {
            this.context.storedPackets().add(packet);
        }
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {

    }

    @Override
    public void onClientOptions(ClientOptionsC2SPacket packet) {
        this.context.options().setValue(packet.options());
    }

    @Override
    public final void onDisconnected(Text reason) {
        this.context.storedPackets().clear();
        this.handleDisconnect(reason);
    }

    public final ClientConnection getConnection() {
        return this.context.connection();
    }

    public final void disconnect(Text reason) {
        try {
            CommonImpl.LOGGER.info("Disconnecting {} on {}: {}", this.getConnectionInfo(), this.getId(), reason.getString());
            this.sendPacket(new DisconnectS2CPacket(reason));
            this.context.connection().disconnect(reason);
            this.context.storedPackets().clear();
        } catch (Exception var3) {
            CommonImpl.LOGGER.error("Error whilst disconnecting player", var3);
        }

    }

    public final MinecraftServer getServer() {
        return this.context.server();
    }

    public final void continueJoining() {
        if (this.canContinue) {
            this.canContinue = false;
            this.sendKeepAlive();
        }
    }

    public final String getConnectionInfo() {
        if (this.getGameProfile() != null) {
            GameProfile var10000 = this.getGameProfile() ;
            return "" + var10000 + " (" + this.context.connection().getAddress() + ")";
        } else {
            return String.valueOf(this.context.connection().getAddress());
        }
    }

    @Override
    public boolean isConnectionOpen() {
        return this.getConnection().isOpen();
    }

    @Override
    public void onReady(ReadyC2SPacket packet) {

    }

    @Override
    public void onSelectKnownPacks(SelectKnownPacksC2SPacket packet) {

    }

    @Override
    public void onCookieResponse(CookieResponseC2SPacket packet) {

    }

    @ApiStatus.NonExtendable
    public interface Context {
        MinecraftServer server();
        GameProfile profile();
    }

    @Override
    public final @Nullable ServerPlayerEntity getPlayerForPacketTweaker() {
        return null;
    }

    public final GameProfile getGameProfile() {
        return this.context.profile();
    }

    @Override
    public GameProfile getGameProfileForPacketTweaker() {
        return this.context.profile();
    }

    @Override
    public SyncedClientOptions getClientOptionsForPacketTweaker() {
        return this.context.options().getValue();
    }

    protected final ServerLoginNetworkHandler getLoginNetworkHandler() {
        return this.context.loginHandler();
    }
}
