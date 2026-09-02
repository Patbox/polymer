package eu.pb4.polymer.networking.impl;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.common.*;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundAcceptCodeOfConductPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.server.level.ClientInformation;

import java.util.function.Consumer;

public record FallbackServerPacketHandler(ConnectionProtocol phase,
                                          Consumer<ClientInformation> optionsConsumer,
                                          Consumer<ServerboundCustomPayloadPacket> payloadConsumer,
                                          Consumer<DisconnectionDetails> disconnectionInfoConsumer
) implements ServerConfigurationPacketListener, ServerGamePacketListener {
    @Override
    public ConnectionProtocol protocol() {
        return phase;
    }

    @Override
    public void handlePunch(ServerboundPunchPacket packet) {

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
    public void handleKeepAlive(ServerboundKeepAlivePacket packet) {

    }

    @Override
    public void handlePong(ServerboundPongPacket packet) {

    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packet) {
        this.payloadConsumer.accept(packet);
    }

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packet) {

    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packet) {
        optionsConsumer.accept(packet.information());
    }

    @Override
    public void handleCustomClickAction(ServerboundCustomClickActionPacket customClickActionC2SPacket) {

    }

    @Override
    public void handleCookieResponse(ServerboundCookieResponsePacket packet) {

    }

    @Override
    public void onDisconnect(DisconnectionDetails info) {
        this.disconnectionInfoConsumer.accept(info);
    }

    @Override
    public boolean isAcceptingMessages() {
        return true;
    }

    @Override
    public void handleChat(ServerboundChatPacket packet) {

    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket packet) {

    }

    @Override
    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket packet) {

    }

    @Override
    public void handleChatAck(ServerboundChatAckPacket packet) {

    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket packet) {

    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) {

    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packet) {

    }

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) {

    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket packet) {

    }

    @Override
    public void handleAttack(ServerboundAttackPacket packet) {

    }

    @Override
    public void handleInteract(ServerboundInteractPacket packet) {

    }

    @Override
    public void handleSpectatorAction(ServerboundSpectatorActionPacket packet) {

    }


    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packet) {

    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) {

    }

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packet) {

    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) {

    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket packet) {

    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) {

    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) {

    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packet) {

    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket packet) {

    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket packet) {

    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) {

    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) {

    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) {

    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) {

    }

    @Override
    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket packet) {

    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) {

    }

    @Override
    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket packet) {

    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) {

    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) {

    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) {

    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) {

    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) {

    }

    @Override
    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket packet) {

    }

    @Override
    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket packet) {

    }

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket packet) {

    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) {

    }

    @Override
    public void handleSetGameRule(ServerboundSetGameRulePacket packet) {

    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) {

    }

    @Override
    public void handleSetTestBlock(ServerboundSetTestBlockPacket packet) {

    }

    @Override
    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket packet) {

    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket packet) {

    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket packet) {

    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket packet) {

    }

    @Override
    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket packet) {

    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket packet) {

    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) {

    }

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) {

    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) {

    }

    @Override
    public void handleChangeGameMode(ServerboundChangeGameModePacket packet) {

    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) {

    }

    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) {

    }

    @Override
    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket packet) {

    }

    @Override
    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket packet) {

    }

    @Override
    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket packet) {

    }

    @Override
    public void handleClientTickEnd(ServerboundClientTickEndPacket packet) {

    }

    @Override
    public void handlePingRequest(ServerboundPingRequestPacket packet) {

    }
}
