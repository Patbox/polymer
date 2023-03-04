package eu.pb4.polymertest.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Unique
    private List<Entity> lastPassengers = Collections.emptyList();

    @Inject(method = "onPlayerInput", at = @At("TAIL"))
    private void polymtest_hrte(PlayerInputC2SPacket packet, CallbackInfo ci) {
        this.player.sendMessage(Text.of("F: " + packet.getForward() + "  S:" + packet.getSideways()), true);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void sendPassengers(CallbackInfo ci) {
        List<Entity> list = this.player.getPassengerList();
        if (!list.equals(this.lastPassengers)) {
            this.sendPacket(new EntityPassengersSetS2CPacket(this.player));
            this.lastPassengers = list;
        }
    }
}
