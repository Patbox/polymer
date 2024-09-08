package eu.pb4.polymertest.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    @Shadow public ServerPlayerEntity player;

    @Unique
    private List<Entity> lastPassengers = Collections.emptyList();

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }


    @Inject(method = "onPlayerInteractItem", at = @At("TAIL"))
    private void polymtest_itemUse(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        this.player.sendMessage(Text.of("ItemUse: " + " Hand|" + packet.getHand() + " Pitch|" + + packet.getPitch() + " Yaw|" + packet.getYaw() + " Seq|" + packet.getSequence()), false);
    }

    @Inject(method = "onPlayerInteractBlock", at = @At("TAIL"))
    private void polymtest_blockUse(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        this.player.sendMessage(Text.of("BlockUse: " + " Hand|" + packet.getHand() + " Pos|" + packet.getBlockHitResult().getBlockPos() + " Seq|" + packet.getSequence()), false);
    }

    @Inject(method = "onPlayerInput", at = @At("TAIL"))
    private void polymtest_hrte(PlayerInputC2SPacket packet, CallbackInfo ci) {
        if (this.player.getMainHandStack().isOf(Items.STICK)) {
            var text = Text.empty();
            text.append(Text.literal("^").formatted(packet.input().forward() ? Formatting.GREEN : Formatting.DARK_GRAY));
            text.append(Text.literal("v").formatted(packet.input().backward() ? Formatting.GREEN : Formatting.DARK_GRAY));
            text.append(Text.literal("<").formatted(packet.input().left() ? Formatting.GREEN : Formatting.DARK_GRAY));
            text.append(Text.literal(">").formatted(packet.input().right() ? Formatting.GREEN : Formatting.DARK_GRAY));
            text.append(Text.literal("-").formatted(packet.input().jump() ? Formatting.GREEN : Formatting.DARK_GRAY));
            text.append(Text.literal("_").formatted(packet.input().sneak() ? Formatting.GREEN : Formatting.DARK_GRAY));
            text.append(Text.literal("$").formatted(packet.input().sprint() ? Formatting.GREEN : Formatting.DARK_GRAY));
            this.player.sendMessage(text, true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void sendPassengers(CallbackInfo ci) {
        List<Entity> list = this.player.getPassengerList();
        if (!list.equals(this.lastPassengers)) {
            this.sendPacket(new EntityPassengersSetS2CPacket(this.player));
            this.lastPassengers = list;
        }
    }

    @Inject(method = "onHandSwing", at = @At("TAIL"))
    private void onSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        //this.player.sendMessage(Text.literal("Swing!"));
    }
}
