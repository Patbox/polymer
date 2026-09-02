package eu.pb4.polymertest.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonPacketListenerImpl {
    @Shadow public ServerPlayer player;

    @Unique
    private List<Entity> lastPassengers = Collections.emptyList();

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }


    /*@Inject(method = "handleUseItem", at = @At("TAIL"))
    private void polymtest_itemUse(ServerboundUseItemPacket packet, CallbackInfo ci) {
        this.player.sendSystemMessage(Component.nullToEmpty("ItemUse: " + " Hand|" + packet.getHand() + " Pitch|" + + packet.getXRot() + " Yaw|" + packet.getYRot() + " Seq|" + packet.getSequence()), false);
    }

    @Inject(method = "handleUseItemOn", at = @At("TAIL"))
    private void polymtest_blockUse(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        this.player.sendSystemMessage(Component.nullToEmpty("BlockUse: " + " Hand|" + packet.getHand() + " Pos|" + packet.getHitResult().getBlockPos() + " Seq|" + packet.getSequence()), false);
    }*/

    @Inject(method = "handlePlayerInput", at = @At("TAIL"))
    private void polymtest_hrte(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
        if (this.player.getMainHandItem().is(Items.STICK)) {
            var text = Component.empty();
            text.append(Component.literal("^").withStyle(packet.input().forward() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            text.append(Component.literal("v").withStyle(packet.input().backward() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            text.append(Component.literal("<").withStyle(packet.input().left() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            text.append(Component.literal(">").withStyle(packet.input().right() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            text.append(Component.literal("-").withStyle(packet.input().jump() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            text.append(Component.literal("_").withStyle(packet.input().shift() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            text.append(Component.literal("$").withStyle(packet.input().sprint() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            this.player.sendSystemMessage(text, true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void sendPassengers(CallbackInfo ci) {
        List<Entity> list = this.player.getPassengers();
        if (!list.equals(this.lastPassengers)) {
            this.send(new ClientboundSetPassengersPacket(this.player));
            this.lastPassengers = list;
        }
    }

    //@Inject(method = "handleAnimate", at = @At("TAIL"))
    //private void onSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        //this.player.sendMessage(Text.literal("Swing!"));
    //}
}
