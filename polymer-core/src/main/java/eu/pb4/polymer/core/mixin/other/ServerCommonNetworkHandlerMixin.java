package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.impl.interfaces.PolymerCommonNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.networking.PacketPatcher;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.ScheduledPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin implements PolymerCommonNetworkHandlerExtension {

    @Unique
    private int polymerCore$tick;

    @Unique
    private final Object2ObjectMap<String, DelayedAction> polymer$delayedActions = new Object2ObjectArrayMap<>();
    @Unique
    private ArrayList<ScheduledPacket> polymer$scheduledPackets = new ArrayList<>();

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Override
    public void polymer$schedulePacket(Packet<?> packet, int duration) {
        this.polymer$scheduledPackets.add(new ScheduledPacket(packet, this.polymerCore$tick + duration));
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void polymer$sendScheduledPackets(CallbackInfo ci) {
        if (!this.polymer$scheduledPackets.isEmpty()) {
            var array = this.polymer$scheduledPackets;
            this.polymer$scheduledPackets = new ArrayList<>();

            for (var entry : array) {
                if (entry.time() <= this.polymerCore$tick) {
                    this.sendPacket(entry.packet());
                } else {
                    this.polymer$scheduledPackets.add(entry);
                }
            }
        }

        if (!this.polymer$delayedActions.isEmpty()) {
            this.polymer$delayedActions.entrySet().removeIf(e -> e.getValue().tryDoing());
        }
        this.polymerCore$tick++;

    }

    @Override
    public void polymer$delayAction(String identifier, int delay, Runnable action) {
        this.polymer$delayedActions.put(identifier, new DelayedAction(identifier, delay, action));
    }


    @ModifyVariable(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
    private Packet<?> polymer$replacePacket(Packet<ClientPlayPacketListener> packet) {
        return PacketPatcher.replace((ServerCommonNetworkHandler) (Object) this, packet);
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void polymer$skipPackets(Packet<ClientPlayPacketListener> packet, PacketCallbacks arg, CallbackInfo ci) {
        if (PacketPatcher.prevent((ServerCommonNetworkHandler) (Object) this, packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("TAIL"))
    private void polymer$extra(Packet<ClientPlayPacketListener> packet, PacketCallbacks arg, CallbackInfo ci) {
        PacketPatcher.sendExtra((ServerCommonNetworkHandler) (Object) this, packet);
    }
}
