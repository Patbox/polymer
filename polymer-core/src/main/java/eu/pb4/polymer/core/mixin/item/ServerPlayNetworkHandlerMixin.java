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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
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

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1200)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler implements LastActionResultStorer {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void onPlayerInteractItem(PlayerInteractItemC2SPacket packet);

    @Shadow
    private int sequence;

    @Shadow public abstract void updateSequence(int sequence);

    @Shadow protected abstract boolean handlePendingTeleport();

    @Unique
    private String polymerCore$language;
    @Unique
    @Nullable
    private ActionResult lastActionResult = null;
    @Unique
    @Nullable
    private ActionSource lastActionSource = null;

    @Unique
    private final EnumSet<Hand> itemActionUsedHands = EnumSet.noneOf(Hand.class);


    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymerCore$storeLanguage(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        this.polymerCore$language = clientData.syncedOptions().language();
    }

    @Inject(method = "onClientOptions", at = @At("TAIL"))
    private void polymerCore$resendLanguage(ClientOptionsC2SPacket packet, CallbackInfo ci) {
        if (CommonImplUtils.isMainPlayer(this.player)) {
            return;
        }

        if (!this.polymerCore$language.equals(packet.options().language())) {
            this.polymerCore$language = packet.options().language();
            PolymerServerProtocol.sendSyncPackets(player.networkHandler, true);
            this.sendPacket(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.player.getWorld().getServer().getCombinedDynamicRegistries())));
            this.player.getRecipeBook().sendInitRecipesPacket(this.player);
        }
    }

    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void polymer$resendHandOnPlace(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        ItemStack itemStack = this.player.getStackInHand(packet.getHand());

        if (this.lastActionResult != null && this.lastActionResult != ActionResult.PASS) {
            ci.cancel();
            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), packet.getHand() == Hand.MAIN_HAND ? 36 + this.player.getInventory().getSelectedSlot() : 45, itemStack));
            return;
        }

        if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, itemStack.getItem()) instanceof PolymerItem polymerItem) {
            var data = PolymerItemUtils.getItemSafely(polymerItem, itemStack, PacketContext.create(this.player));
            if (data.item() instanceof BlockItem || data.item() instanceof BucketItem) {
                this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), packet.getHand() == Hand.MAIN_HAND ? 36 + this.player.getInventory().getSelectedSlot() : 45, itemStack));
            }
        }
    }

    @WrapOperation(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult captureBlockInteraction(ServerPlayerInteractionManager instance, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, Operation<ActionResult> operation, @Local ServerWorld serverWorld) {
        var oldState = this.player.getWorld().getBlockState(hitResult.getBlockPos());

        ScopedOverride soundOverride;
        if (PolymerBlockUtils.isIgnoringPlaySoundExceptedEntity(this.player, stack, hand, oldState, hitResult, serverWorld)) {
            soundOverride = PolymerUtils.ignorePlaySoundExclusion();
        } else {
            soundOverride = ScopedOverride.NO_OP;
        }

        var original = operation.call(instance, player, world, stack, hand, hitResult);
        soundOverride.close();

        if (PolymerBlockUtils.isPolymerBlockInteraction(this.player, stack, hand, oldState, hitResult, serverWorld, original)) {
            if (original instanceof ActionResult.Success success && success.swingSource() == ActionResult.SwingSource.CLIENT) {
                original = new ActionResult.Success(ActionResult.SwingSource.SERVER, success.itemContext());
            }

            this.lastActionResult = original;
            this.lastActionSource = ActionSource.BLOCK;
        }
        return original;
    }

    @Inject(method = "onPlayerInteractItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void preventItemUse(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        if (this.lastActionResult != null && this.lastActionResult != ActionResult.PASS) {
            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), packet.getHand() == Hand.MAIN_HAND ? 36 + this.player.getInventory().getSelectedSlot() : 45, this.player.getStackInHand(packet.getHand())));
            this.server.execute(() -> this.updateSequence(packet.getSequence()));
            ci.cancel();
        }
    }


    @WrapOperation(method = "onPlayerInteractItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactItem(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult captureItemInteraction(ServerPlayerInteractionManager instance, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, Operation<ActionResult> operation, @Local ServerWorld serverWorld) {
        ScopedOverride soundOverride;
        if (PolymerItemUtils.isIgnoringPlaySoundExceptedEntity(this.player, stack, hand, serverWorld)) {
            soundOverride = PolymerUtils.ignorePlaySoundExclusion();
        } else {
            soundOverride = ScopedOverride.NO_OP;
        }

        var original = operation.call(instance, player, world, stack, hand);
        soundOverride.close();

        if (PolymerItemUtils.isPolymerItemInteraction(this.player, stack, hand, serverWorld, original)) {
            if (original instanceof ActionResult.Success success && success.swingSource() == ActionResult.SwingSource.CLIENT) {
                original = new ActionResult.Success(ActionResult.SwingSource.SERVER, success.itemContext());
            }
            this.lastActionResult = original;
            this.lastActionSource = ActionSource.ITEM;
        }
        this.itemActionUsedHands.add(hand);
        return original;
    }

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void preventEntityUse(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (this.lastActionResult != null && this.lastActionResult != ActionResult.PASS) {
            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), this.player.getInventory().getSelectedSlot(), this.player.getStackInHand(Hand.MAIN_HAND)));
            ci.cancel();
        }
    }

    @Inject(method = "onClientTickEnd", at = @At("TAIL"))
    private void onClientTickEndedPolymer(ClientTickEndC2SPacket packet, CallbackInfo ci) {
        if (this.lastActionSource != ActionSource.ITEM && this.lastActionResult == ActionResult.PASS) {
            try {
                var seq = this.sequence != -1 ? this.sequence : Integer.MAX_VALUE;
                for (var hand : Hand.values()) {
                    if (!this.itemActionUsedHands.contains(hand)) {
                        this.onPlayerInteractItem(new PlayerInteractItemC2SPacket(hand, seq, this.player.getYaw(), this.player.getPitch()));
                    }
                }
                this.itemActionUsedHands.clear();
            } catch (Throwable e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }

        if (this.lastActionSource != null) {
            var f = LivingEntityAccessor.getLIVING_FLAGS();
            this.sendPacket(new EntityTrackerUpdateS2CPacket(this.player.getId(),
                    List.of(DataTracker.SerializedEntry.of(f, this.player.getDataTracker().get(f))
                    )));

            this.lastActionSource = null;
        }

        this.lastActionResult = null;
    }


    @Inject(method = "onPlayerInteractBlock", at = @At("TAIL"))
    private void polymer$updateMoreBlocks(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        if (PolymerImpl.RESEND_BLOCKS_AROUND_CLICK) {
            var base = packet.getBlockHitResult().getBlockPos();
            for (Direction direction : Direction.values()) {
                BlockPacketUtil.sendUpdate(this.player, base.offset(direction));
            }
        }
    }

    @Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$1")
    public static class EntityHandlerMixin {
        @Shadow
        @Final
        ServerPlayNetworkHandler field_28963;
        @Shadow
        @Final
        Entity field_28962;

        @ModifyExpressionValue(method = "processInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler$Interaction;run(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
        private ActionResult captureIEntityInteraction(ActionResult original, @Local(argsOnly = true) Hand hand) {
            if (PolymerEntityUtils.isPolymerEntityInteraction(this.field_28963.player, hand, this.field_28963.player.getStackInHand(hand), (ServerWorld) this.field_28962.getWorld(), this.field_28962, original)) {
                ((LastActionResultStorer) this.field_28963).polymer$setLastActionResult(original);
                ((LastActionResultStorer) this.field_28963).polymer$setLastActionSource(ActionSource.ENTITY);
            }
            return original;
        }
    }


    public void polymer$setLastActionResult(ActionResult lastActionResult) {
        this.lastActionResult = lastActionResult;
    }

    @Override
    public void polymer$setLastActionSource(ActionSource source) {
        this.lastActionSource = source;
    }
}
