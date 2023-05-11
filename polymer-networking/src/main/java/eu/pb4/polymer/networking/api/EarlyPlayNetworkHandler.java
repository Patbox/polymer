package eu.pb4.polymer.networking.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.impl.EarlyConnectionMagic;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PlayerProvidingPacketListener;

import java.util.Optional;
import java.util.function.Function;

/**
 * This api exposes Polymer's early play packets utilities.
 *
 * Use carefully, as client might not be initialized, or it might have leftover state from previous EarlyPlay handlers
 * Use this only if you know what you are doing and you need to do sync/packets before player joins a world.
 */

public abstract class EarlyPlayNetworkHandler implements ServerPlayPacketListener, TickablePacketListener, PlayerProvidingPacketListener {

    public static void register(Function<Context, EarlyPlayNetworkHandler> constructor) {
        EarlyConnectionMagic.register(constructor);
    }

    private final EarlyConnectionMagic.ContextImpl context;
    private final Identifier identifier;

    private volatile long lastRespose = 0;

    private volatile int keepAliveSent = 0;
    private volatile int keepAliveReceived = 0;
    private volatile int pingsId = 1024;
    private volatile boolean canContinue = true;
    private volatile boolean alreadyContinued;

    public EarlyPlayNetworkHandler(Identifier identifier, Context context) {
        this.context = (EarlyConnectionMagic.ContextImpl) context;
        this.identifier = identifier;

        this.context.connection().setPacketListener(this);
        this.context.connection().setState(NetworkState.PLAY);

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
        if (this.lastRespose++ == 1200) {
            this.disconnect(Text.translatable("multiplayer.disconnect.slow_login"));
        } else if (this.lastRespose == 20) {
            this.sendKeepAlive();
        }

        this.onTick();
    }

    protected void onTick() {
    }

    protected void forceRespawnPacket() {
        ((TempPlayerLoginAttachments) this.getPlayer()).polymerNet$setForceRespawnPacket();
    }

    protected boolean isForcingRespawnPacket() {
        return ((TempPlayerLoginAttachments) this.getPlayer()).polymerNet$getForceRespawnPacket();
    }

    @Override
    public final void onKeepAlive(KeepAliveC2SPacket packet) {
        this.lastRespose = -20;
        this.keepAliveReceived++;
        if (this.canContinue) {
            this.handleKeepAlive(packet.getId());
        } else if (!this.alreadyContinued && this.keepAliveReceived >= this.keepAliveSent) {
            this.alreadyContinued = true;
            this.context.server().execute(() -> this.context.continueRunning().accept(this.context));
        }
    }

    public final void sendPacket(Packet<?> packet) {
        this.context.connection().send(packet);
        if (packet instanceof GameJoinS2CPacket packet1) {
            if (this.isForcingRespawnPacket()) {
                this.context.connection().send(new PlayerRespawnS2CPacket(packet1.dimensionType(), packet1.dimensionId(), packet1.sha256Seed(), packet1.gameMode(), packet1.previousGameMode(), packet1.debugWorld(), packet1.flatWorld(), (byte) 0, packet1.lastDeathLocation(), packet1.portalCooldown()));
            }

            this.forceRespawnPacket();
        }
    }

    public final void sendKeepAlive(long value) {
        this.keepAliveSent++;
        this.sendPacket(new KeepAliveS2CPacket(value));
    }

    public final void sendKeepAlive() {
        this.sendKeepAlive(System.currentTimeMillis());
    }

    public final void sendPing(int id) {
        this.sendPacket(new PlayPingS2CPacket(id));
    }

    public final int sendPing() {
        var id = this.pingsId++;
        this.sendPing(id);
        return id;
    }

    @Override
    public final void onCustomPayload(CustomPayloadC2SPacket packet) {
        if (!handleCustomPayload(packet)) {
            this.context.storedPackets().add(new CustomPayloadC2SPacket(packet.getChannel(), new PacketByteBuf(packet.getData().copy())));
        }
    }

    public final void sendInitialGameJoin() {
        if (!this.isForcingRespawnPacket()) {
            var player = this.getPlayer();
            var server = this.getServer();
            this.sendPacket(new GameJoinS2CPacket(player.getId(), false, GameMode.SPECTATOR, null, server.getWorldRegistryKeys(), server.getRegistryManager(), server.getOverworld().getDimensionKey(), server.getOverworld().getRegistryKey(), 0, server.getPlayerManager().getMaxPlayerCount(), 2, 2, false, false, false, true, Optional.empty(), 0));
        }
    }

    @Override
    public final void onDisconnected(Text reason) {
        for (var packets : this.context.storedPackets()) {
            packets.getData().release();
        }
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

            for (var packets : this.context.storedPackets()) {
                packets.getData().release();
            }
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

    public final ServerPlayerEntity getPlayer() {
        return this.context.player();
    }

    public final String getConnectionInfo() {
        if (this.getPlayer().getGameProfile() != null) {
            GameProfile var10000 = this.getPlayer().getGameProfile() ;
            return "" + var10000 + " (" + this.context.connection().getAddress() + ")";
        } else {
            return String.valueOf(this.context.connection().getAddress());
        }
    }

    @Override
    public void onPong(PlayPongC2SPacket packet) {

    }

    @Override
    public void onHandSwing(HandSwingC2SPacket packet) {

    }

    @Override
    public void onChatMessage(ChatMessageC2SPacket packet) {

    }

    @Override
    public void onCommandExecution(CommandExecutionC2SPacket packet) {

    }

    @Override
    public void onMessageAcknowledgment(MessageAcknowledgmentC2SPacket packet) {

    }

    @Override
    public void onClientStatus(ClientStatusC2SPacket packet) {

    }

    @Override
    public void onClientSettings(ClientSettingsC2SPacket packet) {

    }

    @Override
    public void onButtonClick(ButtonClickC2SPacket packet) {

    }

    @Override
    public void onClickSlot(ClickSlotC2SPacket packet) {

    }

    @Override
    public void onCraftRequest(CraftRequestC2SPacket packet) {

    }

    @Override
    public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {

    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {

    }

    @Override
    public void onPlayerMove(PlayerMoveC2SPacket packet) {

    }


    @Override
    public void onUpdatePlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {

    }

    @Override
    public void onPlayerAction(PlayerActionC2SPacket packet) {

    }

    @Override
    public void onClientCommand(ClientCommandC2SPacket packet) {

    }

    @Override
    public void onPlayerInput(PlayerInputC2SPacket packet) {

    }

    @Override
    public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {

    }

    @Override
    public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {

    }

    @Override
    public void onUpdateSign(UpdateSignC2SPacket packet) {

    }

    @Override
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {

    }

    @Override
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {

    }

    @Override
    public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {

    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {

    }

    @Override
    public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {

    }

    @Override
    public void onVehicleMove(VehicleMoveC2SPacket packet) {

    }

    @Override
    public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {

    }

    @Override
    public void onRecipeBookData(RecipeBookDataC2SPacket packet) {

    }

    @Override
    public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {

    }

    @Override
    public void onAdvancementTab(AdvancementTabC2SPacket packet) {

    }

    @Override
    public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {

    }

    @Override
    public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {

    }

    @Override
    public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {

    }

    @Override
    public void onPickFromInventory(PickFromInventoryC2SPacket packet) {

    }

    @Override
    public void onRenameItem(RenameItemC2SPacket packet) {

    }

    @Override
    public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {

    }

    @Override
    public void onUpdateStructureBlock(UpdateStructureBlockC2SPacket packet) {

    }

    @Override
    public void onSelectMerchantTrade(SelectMerchantTradeC2SPacket packet) {

    }

    @Override
    public void onBookUpdate(BookUpdateC2SPacket packet) {

    }

    @Override
    public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {

    }

    @Override
    public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {

    }

    @Override
    public void onUpdateJigsaw(UpdateJigsawC2SPacket packet) {

    }

    @Override
    public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {

    }

    @Override
    public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {

    }

    @Override
    public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {

    }

    @Override
    public void onPlayerSession(PlayerSessionC2SPacket packet) {

    }

    @Override
    public boolean isConnectionOpen() {
        return this.getConnection().isOpen();
    }

    @ApiStatus.NonExtendable
    public interface Context {}

    @Override
    public final @Nullable ServerPlayerEntity getPlayerForPacketTweaker() {
        return this.getPlayer();
    }
}
