package eu.pb4.polymer.networking.api.server;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.impl.EarlyConfigurationConnectionMagic;
import eu.pb4.polymer.networking.mixin.ConnectionAccessor;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * This api exposes Polymer's early play packets utilities.
 * <p>
 * Use carefully, as client might not be initialized, or it might have leftover state from previous EarlyPlay handlers
 * Use this only if you know what you are doing and you need to do sync/packets before player joins a world.
 */

public class EarlyConfigurationNetworkHandler implements ServerConfigurationPacketListener, TickablePacketListener, PacketContextProvider {

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

        ((ConnectionAccessor) this.context.connection()).setPacketListener(this);
        this.sendKeepAlive();
    }

    public static void register(Function<Context, EarlyConfigurationNetworkHandler> constructor) {
        EarlyConfigurationConnectionMagic.register(constructor);
    }

    public final Identifier getId() {
        return this.identifier;
    }

    public void handleDisconnect(DisconnectionDetails reason) {

    }

    public void handleKeepAlive(long time) {

    }

    public boolean tryHandleCustomPayload(ServerboundCustomPayloadPacket packet) {
        return false;
    }


    @Override
    public final void tick() {
        if (this.lastResponse == 1200) {
            this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
        } else if (this.lastResponse == 20) {
            this.sendKeepAlive();
        }
        this.lastResponse++;
        this.onTick();
    }

    protected void onTick() {
    }

    @Override
    public final void handleKeepAlive(ServerboundKeepAlivePacket packet) {
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
    public void handlePong(ServerboundPongPacket packet) {

    }

    public final void sendPacket(Packet<?> packet) {
        this.context.connection().send(packet);
    }

    protected void sendPacket(CustomPacketPayload payload) {
        this.sendPacket(new ClientboundCustomPayloadPacket(payload));
    }

    public final void sendKeepAlive(long value) {
        this.keepAliveSent++;
        this.sendPacket(new ClientboundKeepAlivePacket(value));
    }

    public final void sendKeepAlive() {
        this.sendKeepAlive(System.currentTimeMillis());
    }

    public final void sendPing(int id) {
        this.sendPacket(new ClientboundPingPacket(id));
    }

    public final int sendPing() {
        var id = this.pingsId++;
        this.sendPing(id);
        return id;
    }

    @Override
    public final void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        if (!tryHandleCustomPayload(packet)) {
            this.context.storedPackets().add(packet);
        }
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {

    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packet) {
        this.context.options().set(packet.information());
    }

    @Override
    public void handleCustomClickAction(ServerboundCustomClickActionPacket customClickActionC2SPacket) {

    }

    @Override
    public void onDisconnect(DisconnectionDetails info) {
        this.context.storedPackets().clear();
        this.handleDisconnect(info);
    }

    public final Connection getConnection() {
        return this.context.connection();
    }

    public final void disconnect(Component reason) {
        try {
            CommonImpl.LOGGER.info("Disconnecting {} on {}: {}", this.getConnectionInfo(), this.getId(), reason.getString());
            this.sendPacket(new ClientboundDisconnectPacket(reason));
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
            GameProfile var10000 = this.getGameProfile();
            return var10000 + " (" + this.context.connection().getRemoteAddress() + ")";
        } else {
            return String.valueOf(this.context.connection().getRemoteAddress());
        }
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.getConnection().isConnected();
    }

    @Override
    public void handleConfigurationFinished(ServerboundFinishConfigurationPacket packet) {

    }

    @Override
    public void handleSelectKnownPacks(ServerboundSelectKnownPacks packet) {

    }

    @Override
    public void handleAcceptCodeOfConduct(ServerboundAcceptCodeOfConductPacket packet) {

    }

    @Override
    public void handleCookieResponse(ServerboundCookieResponsePacket packet) {

    }

    public final GameProfile getGameProfile() {
        return this.context.profile();
    }

    @Override
    public PacketContext getPacketContext() {
        return this.context.connection().getPacketContext();
    }

    protected final ServerLoginPacketListenerImpl getLoginNetworkHandler() {
        return this.context.loginHandler();
    }

    @ApiStatus.NonExtendable
    public interface Context {
        MinecraftServer server();

        GameProfile profile();
    }
}
