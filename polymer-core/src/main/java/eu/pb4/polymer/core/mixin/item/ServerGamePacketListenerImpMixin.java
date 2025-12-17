package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.ScopedOverride;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.LastActionResultStorer;
import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.other.ActionSource;
import eu.pb4.polymer.core.mixin.entity.LivingEntityAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.*;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.EnumSet;
import java.util.List;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1200)
public abstract class ServerGamePacketListenerImpMixin extends ServerCommonPacketListenerImpl implements LastActionResultStorer {
    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void handleUseItem(ServerboundUseItemPacket packet);

    @Shadow
    private int ackBlockChangesUpTo;

    @Shadow public abstract void ackBlockChangesUpTo(int sequence);

    @Shadow protected abstract boolean updateAwaitingTeleport();

    @Unique
    private String polymerCore$language;
    @Unique
    @Nullable
    private InteractionResult lastActionResult = null;
    @Unique
    @Nullable
    private ActionSource lastActionSource = null;

    @Unique
    private final EnumSet<InteractionHand> itemActionUsedHands = EnumSet.noneOf(InteractionHand.class);


    public ServerGamePacketListenerImpMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymerCore$storeLanguage(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        this.polymerCore$language = clientData.clientInformation().language();
    }

    @Inject(method = "handleClientInformation", at = @At("TAIL"))
    private void polymerCore$resendLanguage(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        if (CommonImplUtils.isMainPlayer(this.player)) {
            return;
        }

        if (!this.polymerCore$language.equals(packet.information().language())) {
            this.polymerCore$language = packet.information().language();
            PolymerServerProtocol.sendSyncPackets(player.connection, true);
            this.send(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(this.player.level().getServer().registries())));
            this.player.getRecipeBook().sendInitialRecipeBook(this.player);
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true)
    private void polymer$resendHandOnPlace(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        ItemStack itemStack = this.player.getItemInHand(packet.getHand());

        if (this.lastActionResult != null && this.lastActionResult != InteractionResult.PASS) {
            ci.cancel();
            this.send(new ClientboundContainerSetSlotPacket(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(), packet.getHand() == InteractionHand.MAIN_HAND ? 36 + this.player.getInventory().getSelectedSlot() : 45, itemStack));
            return;
        }

        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
            var data = PolymerItemUtils.getItemSafely(polymerItem, itemStack, PacketContext.create(this.player));
            if (data.item() instanceof BlockItem || data.item() instanceof BucketItem) {
                this.send(new ClientboundContainerSetSlotPacket(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(), packet.getHand() == InteractionHand.MAIN_HAND ? 36 + this.player.getInventory().getSelectedSlot() : 45, itemStack));
            }
        }
    }

    @WrapOperation(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult captureBlockInteraction(ServerPlayerGameMode instance, ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult hitResult, Operation<InteractionResult> operation, @Local ServerLevel serverWorld) {
        var oldState = this.player.level().getBlockState(hitResult.getBlockPos());

        ScopedOverride soundOverride;
        if (PolymerBlockUtils.isIgnoringPlaySoundExceptedEntity(this.player, stack, hand, oldState, hitResult, serverWorld)) {
            soundOverride = PolymerUtils.ignorePlaySoundExclusion();
        } else {
            soundOverride = ScopedOverride.NO_OP;
        }

        var original = operation.call(instance, player, world, stack, hand, hitResult);
        soundOverride.close();

        if (PolymerBlockUtils.isPolymerBlockInteraction(this.player, stack, hand, oldState, hitResult, serverWorld, original)) {
            if (original instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                original = new InteractionResult.Success(InteractionResult.SwingSource.SERVER, success.itemContext());
            }

            this.lastActionResult = original;
            this.lastActionSource = ActionSource.BLOCK;
        }
        return original;
    }

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true)
    private void preventItemUse(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (this.lastActionResult != null && this.lastActionResult != InteractionResult.PASS) {
            this.send(new ClientboundContainerSetSlotPacket(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(), packet.getHand() == InteractionHand.MAIN_HAND ? 36 + this.player.getInventory().getSelectedSlot() : 45, this.player.getItemInHand(packet.getHand())));
            this.server.execute(() -> this.ackBlockChangesUpTo(packet.getSequence()));
            ci.cancel();
        }
    }


    @WrapOperation(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult captureItemInteraction(ServerPlayerGameMode instance, ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, Operation<InteractionResult> operation, @Local ServerLevel serverWorld) {
        ScopedOverride soundOverride;
        if (PolymerItemUtils.isIgnoringPlaySoundExceptedEntity(this.player, stack, hand, serverWorld)) {
            soundOverride = PolymerUtils.ignorePlaySoundExclusion();
        } else {
            soundOverride = ScopedOverride.NO_OP;
        }

        var original = operation.call(instance, player, world, stack, hand);
        soundOverride.close();

        if (PolymerItemUtils.isPolymerItemInteraction(this.player, stack, hand, serverWorld, original)) {
            if (original instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                original = new InteractionResult.Success(InteractionResult.SwingSource.SERVER, success.itemContext());
            }
            this.lastActionResult = original;
            this.lastActionSource = ActionSource.ITEM;
        }
        this.itemActionUsedHands.add(hand);
        return original;
    }

    @Inject(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true)
    private void preventEntityUse(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.lastActionResult != null && this.lastActionResult != InteractionResult.PASS) {
            this.send(new ClientboundContainerSetSlotPacket(this.player.inventoryMenu.containerId, this.player.inventoryMenu.incrementStateId(), this.player.getInventory().getSelectedSlot(), this.player.getItemInHand(InteractionHand.MAIN_HAND)));
            ci.cancel();
        }
    }

    @Inject(method = "handleClientTickEnd", at = @At("TAIL"))
    private void onClientTickEndedPolymer(ServerboundClientTickEndPacket packet, CallbackInfo ci) {
        if (this.lastActionSource != ActionSource.ITEM && this.lastActionResult == InteractionResult.PASS) {
            try {
                var seq = this.ackBlockChangesUpTo != -1 ? this.ackBlockChangesUpTo : Integer.MAX_VALUE;
                for (var hand : InteractionHand.values()) {
                    if (!this.itemActionUsedHands.contains(hand)) {
                        this.handleUseItem(new ServerboundUseItemPacket(hand, seq, this.player.getYRot(), this.player.getXRot()));
                    }
                }
                this.itemActionUsedHands.clear();
            } catch (Throwable e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        if (this.lastActionSource != null) {
            var f = LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS();
            this.send(new ClientboundSetEntityDataPacket(this.player.getId(),
                    List.of(SynchedEntityData.DataValue.create(f, this.player.getEntityData().get(f))
                    )));

            this.lastActionSource = null;
        }

        this.lastActionResult = null;
    }


    @Inject(method = "handleUseItemOn", at = @At("TAIL"))
    private void polymer$updateMoreBlocks(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (PolymerImpl.RESEND_BLOCKS_AROUND_CLICK) {
            var base = packet.getHitResult().getBlockPos();
            for (Direction direction : Direction.values()) {
                BlockPacketUtil.sendUpdate(this.player, base.relative(direction));
            }
        }
    }

    @Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
    public static class EntityHandlerMixin {
        @Shadow
        @Final
        ServerGamePacketListenerImpl field_28963;
        @Shadow
        @Final
        Entity val$target;

        @ModifyExpressionValue(method = "performInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl$EntityInteraction;run(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
        private InteractionResult captureIEntityInteraction(InteractionResult original, @Local(argsOnly = true) InteractionHand hand) {
            if (PolymerEntityUtils.isPolymerEntityInteraction(this.field_28963.player, hand, this.field_28963.player.getItemInHand(hand), (ServerLevel) this.val$target.level(), this.val$target, original)) {
                ((LastActionResultStorer) this.field_28963).polymer$setLastActionResult(original);
                ((LastActionResultStorer) this.field_28963).polymer$setLastActionSource(ActionSource.ENTITY);
            }
            return original;
        }
    }


    public void polymer$setLastActionResult(InteractionResult lastActionResult) {
        this.lastActionResult = lastActionResult;
    }

    @Override
    public void polymer$setLastActionSource(ActionSource source) {
        this.lastActionSource = source;
    }
}
