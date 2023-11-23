package eu.pb4.polymer.networking.api.server;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.impl.EarlyPlayConnectionMagic;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import eu.pb4.polymer.networking.impl.packets.HelloS2CPayload;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.common.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
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

public class EarlyPlayNetworkHandler implements ServerPlayPacketListener, TickablePacketListener, ContextProvidingPacketListener {

    public static void register(Function<Context, EarlyPlayNetworkHandler> constructor) {
        EarlyPlayConnectionMagic.register(constructor);
    }

    private final EarlyPlayConnectionMagic.ContextImpl context;
    private final Identifier identifier;

    private volatile long lastResponse = 0;

    private volatile int keepAliveSent = 0;
    private volatile int keepAliveReceived = 0;
    private volatile int pingsId = 1024;
    private volatile boolean canContinue = true;
    private volatile boolean alreadyContinued;

    public EarlyPlayNetworkHandler(Identifier identifier, Context context) {
        this.context = (EarlyPlayConnectionMagic.ContextImpl) context;
        this.identifier = identifier;

        this.context.connection().setPacketListener(this);

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
        if (this.lastResponse++ == 1200) {
            this.disconnect(Text.translatable("multiplayer.disconnect.slow_login"));
        } else if (this.lastResponse == 20) {
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
        if (packet instanceof GameJoinS2CPacket packet1) {
            if (this.isForcingRespawnPacket()) {
                this.context.connection().send(new PlayerRespawnS2CPacket(packet1.commonPlayerSpawnInfo(), PlayerRespawnS2CPacket.KEEP_ALL));
            }

            this.forceRespawnPacket();
        }
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
        try {
            this.getPlayer().setClientOptions(packet.options());
        } catch (Throwable e) {
            // Ignore
        }

        this.context.options().setValue(packet.options());
    }

    public final void sendInitialGameJoin() {
        if (!this.isForcingRespawnPacket()) {
            var player = this.getPlayer();
            var world = this.getServer().getOverworld();
            this.sendPacket(new GameJoinS2CPacket(player.getId(), false, this.getServer().getWorldRegistryKeys(), 0, 2, 2, true, false, true, new CommonPlayerSpawnInfo(
                    world.getDimensionKey(),
                    world.getRegistryKey(),
                    0,
                    GameMode.ADVENTURE,
                    GameMode.ADVENTURE,
                    false,
                    true,
                    Optional.empty(),
                    0
            )));
        }
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
    public void onSlotChangedState(SlotChangedStateC2SPacket packet) {

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
    public void onAcknowledgeReconfiguration(AcknowledgeReconfigurationC2SPacket packet) {

    }

    @Override
    public void onAcknowledgeChunks(AcknowledgeChunksC2SPacket packet) {

    }

    @Override
    public boolean isConnectionOpen() {
        return this.getConnection().isOpen();
    }

    @Override
    public void onQueryPing(QueryPingC2SPacket packet) {

    }

    @ApiStatus.NonExtendable
    public interface Context {
        MinecraftServer server();
        ServerPlayerEntity player();
    }

    @Override
    public final @Nullable ServerPlayerEntity getPlayerForPacketTweaker() {
        return this.getPlayer();
    }


    @Override
    public GameProfile getGameProfileForPacketTweaker() {
        return this.getPlayer().getGameProfile();
    }

    @Override
    public SyncedClientOptions getClientOptionsForPacketTweaker() {
        return this.context.options().getValue();
    }

    protected final ServerConfigurationNetworkHandler getConfigurationNetworkHandler() {
        return this.context.loginHandler();
    }
}
