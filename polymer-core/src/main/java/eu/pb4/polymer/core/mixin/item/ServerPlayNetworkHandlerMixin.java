package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.LastActionResultStorer;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.core.impl.other.ActionSource;
import eu.pb4.polymer.core.mixin.entity.LivingEntityAccessor;
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
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

@Mixin(value = ServerPlayNetworkHandler.class, priority = 1200)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler implements LastActionResultStorer {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract void onPlayerInteractItem(PlayerInteractItemC2SPacket packet);

    @Shadow
    private int sequence;
    @Unique
    private String polymerCore$language;
    @Unique
    @Nullable
    private ActionResult lastActionResult = null;
    @Unique
    @Nullable
    private ActionSource lastActionSource = null;

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
            this.sendPacket(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.player.getServerWorld().getServer().getCombinedDynamicRegistries())));
            this.player.getRecipeBook().sendInitRecipesPacket(this.player);
        }
    }

    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void polymer$resendHandOnPlace(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        ItemStack itemStack = this.player.getStackInHand(packet.getHand());

        if (this.lastActionResult != null && this.lastActionResult != ActionResult.PASS) {
            ci.cancel();
            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), packet.getHand() == Hand.MAIN_HAND ? 36 + this.player.getInventory().selectedSlot : 45, itemStack));
            return;
        }

        if (itemStack.getItem() instanceof PolymerItem polymerItem) {
            var data = PolymerItemUtils.getItemSafely(polymerItem, itemStack, PacketContext.create(this.player));
            if (data.item() instanceof BlockItem || data.item() instanceof BucketItem) {
                this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), packet.getHand() == Hand.MAIN_HAND ? 36 + this.player.getInventory().selectedSlot : 45, itemStack));
            }
        }
    }


    @ModifyExpressionValue(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult captureBlockInteraction(ActionResult original, @Local ItemStack stack, @Local Hand hand, @Local BlockHitResult blockHitResult, @Local ServerWorld world) {
        if (PolymerBlockUtils.isPolymerBlockInteraction(this.player, stack, hand, blockHitResult, world, original)) {
            this.lastActionResult = original;
            this.lastActionSource = ActionSource.BLOCK;
        }
        return original;
    }

    @Inject(method = "onPlayerInteractItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void preventItemUse(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        if (this.lastActionResult != null && this.lastActionResult != ActionResult.PASS) {
            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), packet.getHand() == Hand.MAIN_HAND ? 36 + this.player.getInventory().selectedSlot : 45, this.player.getStackInHand(packet.getHand())));
            ci.cancel();
        }
    }


    @ModifyExpressionValue(method = "onPlayerInteractItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactItem(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult captureItemInteraction(ActionResult original, @Local ItemStack stack, @Local Hand hand, @Local ServerWorld world) {
        if (PolymerItemUtils.isPolymerItemInteraction(this.player, stack, hand, world, original)) {
            this.lastActionResult = original;
            this.lastActionSource = ActionSource.ITEM;
        }
        return original;
    }

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", shift = At.Shift.AFTER), cancellable = true)
    private void preventEntityUse(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (this.lastActionResult != null && this.lastActionResult != ActionResult.PASS) {
            this.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.playerScreenHandler.syncId, this.player.playerScreenHandler.nextRevision(), this.player.getInventory().selectedSlot, this.player.getStackInHand(Hand.MAIN_HAND)));
            ci.cancel();
        }
    }

    @Inject(method = "onClientTickEnd", at = @At("TAIL"))
    private void onClientTickEndedPolymer(ClientTickEndC2SPacket packet, CallbackInfo ci) {
        if (this.lastActionSource != ActionSource.ITEM && this.lastActionResult == ActionResult.PASS) {
            try {
                var seq = Math.max(this.sequence, 0);
                for (var hand : Hand.values()) {
                    this.onPlayerInteractItem(new PlayerInteractItemC2SPacket(hand, seq, this.player.getYaw(), this.player.getPitch()));
                }
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
                var pos = base.offset(direction);
                var state = player.getServerWorld().getBlockState(pos);
                player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, state));

                if (state.hasBlockEntity()) {
                    var be = player.getWorld().getBlockEntity(pos);
                    if (be != null) {
                        player.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(be));
                    }
                }
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
