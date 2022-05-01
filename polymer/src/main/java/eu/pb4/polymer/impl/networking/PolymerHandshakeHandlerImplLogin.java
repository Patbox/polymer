package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.api.x.BlockMapper;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.interfaces.TempPlayerLoginAttachments;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.*;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class PolymerHandshakeHandlerImplLogin implements PolymerHandshakeHandler, ServerPlayPacketListener {
    public static long MAGIC_VALUE = 0xbb706c6d72627374L;

    private final ClientConnection connection;
    private final MinecraftServer server;
    private final ServerPlayerEntity player;
    private final Consumer<PolymerHandshakeHandlerImplLogin> continueJoining;
    private String polymerVersion = "";
    private Object2IntMap<String> protocolVersions = null;
    private Object2LongMap<String> lastUpdate = new Object2LongOpenHashMap<>();
    private BlockMapper blockMapper;
    private boolean hasPack = false;
    private Collection<CustomPayloadC2SPacket> storedPackets = new ArrayList<>();

    public PolymerHandshakeHandlerImplLogin(MinecraftServer server, ServerPlayerEntity player, ClientConnection connection,
                                            Consumer<PolymerHandshakeHandlerImplLogin> continueJoining) {
        this.server = server;
        this.connection = connection;
        this.player = player;
        this.continueJoining = continueJoining;
        this.connection.setPacketListener(this);
        this.connection.setState(NetworkState.PLAY);
        ((TempPlayerLoginAttachments) player).polymer_setHandshakeHandler(this);
        this.connection.send(new KeepAliveS2CPacket(MAGIC_VALUE));
        this.blockMapper = BlockMapper.getDefault(player);

        PolymerSyncUtils.PREPARE_HANDSHAKE.invoke((c) -> c.accept(this));
    }

    public void sendPacket(Packet<?> packet) {
        this.connection.send(packet);
    }

    public void set(String polymerVersion, Object2IntMap<String> protocolVersions) {
        this.polymerVersion = polymerVersion;
        this.protocolVersions = protocolVersions;
    }

    public boolean isPolymer() {
        return !this.polymerVersion.isEmpty();
    }

    public String getPolymerVersion() {
        return this.polymerVersion;
    }

    public int getSupportedProtocol(String identifier) {
        return this.protocolVersions != null ? this.protocolVersions.getOrDefault(identifier, -1) : -1;
    }

    @Override
    public void setLastPacketTime(String identifier) {
        this.lastUpdate.put(identifier, System.currentTimeMillis());
    }

    @Override
    public long getLastPacketTime(String identifier) {
        return this.lastUpdate.getLong(identifier);
    }

    @Override
    public boolean shouldUpdateWorld() {
        return false;
    }

    @Override
    public BlockMapper getBlockMapper() {
        return this.blockMapper;
    }

    @Override
    public void setBlockMapper(BlockMapper mapper) {
        this.blockMapper = mapper;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public void apply(ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        polymerHandler.polymer_setVersion(this.getPolymerVersion());

        if (this.protocolVersions != null) {
            for (var entry : this.protocolVersions.object2IntEntrySet()) {
                polymerHandler.polymer_setSupportedVersion(entry.getKey(), entry.getIntValue());
            }
        }

        for (var entry : this.lastUpdate.keySet()) {
            polymerHandler.polymer_savePacketTime(entry);
        }

        polymerHandler.polymer_setBlockMapper(this.blockMapper);

        for (var packet : this.storedPackets) {
            packet.apply(handler);
        }
        this.storedPackets.clear();
    }

    @Override
    public boolean getPackStatus() {
        return this.hasPack;
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
        var data = packet.getData();
        if (packet.getChannel().equals(ClientPackets.HANDSHAKE_ID)) {
            PolymerServerProtocolHandler.handleHandshake(this, data.readVarInt(), data);
        } else {
            this.storedPackets.add(new CustomPayloadC2SPacket(packet.getChannel(), new PacketByteBuf(packet.getData().copy())));
        }
    }

    @Override
    public void onKeepAlive(KeepAliveC2SPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.server);

        if (packet.getId() == MAGIC_VALUE) {
            this.continueJoining.accept(this);
        }
    }

    @Override
    public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
        if (PolymerRPUtils.shouldCheckByDefault()) {
            this.hasPack = switch (packet.getStatus()) {
                case ACCEPTED, SUCCESSFULLY_LOADED -> true;
                case DECLINED, FAILED_DOWNLOAD -> false;
            };
        }
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    @Override
    public void onHandSwing(HandSwingC2SPacket packet) {

    }

    @Override
    public void onChatMessage(ChatMessageC2SPacket packet) {

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
    public void onPong(PlayPongC2SPacket packet) {

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
    public void onDisconnected(Text reason) {
        for (var packets : this.storedPackets) {
            packets.getData().release();
        }
        this.storedPackets.clear();
    }

    @Override
    public ClientConnection getConnection() {
        return this.connection;
    }
}