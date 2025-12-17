package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.PolymerCommonPacketListenerExtension;
import eu.pb4.polymer.core.impl.networking.PacketPatcher;
import eu.pb4.polymer.core.impl.other.DelayedAction;
import eu.pb4.polymer.core.impl.other.ScheduledPacket;
import io.netty.channel.ChannelFutureListener;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin implements PolymerCommonPacketListenerExtension {

    @Unique
    private int polymerCore$tick;

    @Unique
    private final Object2ObjectMap<String, DelayedAction> polymer$delayedActions = new Object2ObjectArrayMap<>();
    @Unique
    private ArrayList<ScheduledPacket> polymer$scheduledPackets = new ArrayList<>();

    @Shadow
    public abstract void send(Packet<?> packet);

    @Override
    public void polymer$schedulePacket(Packet<?> packet, int duration) {
        this.polymer$scheduledPackets.add(new ScheduledPacket(packet, this.polymerCore$tick + duration));
    }

    @Inject(method = "keepConnectionAlive", at = @At("TAIL"))
    private void polymer$sendScheduledPackets(CallbackInfo ci) {
        if (!this.polymer$scheduledPackets.isEmpty()) {
            var array = this.polymer$scheduledPackets;
            this.polymer$scheduledPackets = new ArrayList<>();

            for (var entry : array) {
                if (entry.time() <= this.polymerCore$tick) {
                    this.send(entry.packet());
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


    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"))
    private Packet<?> polymer$replacePacket(Packet<ClientGamePacketListener> packet) {
        return PacketPatcher.replace((ServerCommonPacketListenerImpl) (Object) this, packet);
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void polymer$skipPackets(Packet<ClientGamePacketListener> packet, ChannelFutureListener listener, CallbackInfo ci) {
        if (PacketPatcher.prevent((ServerCommonPacketListenerImpl) (Object) this, packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("TAIL"))
    private void polymer$extra(Packet<ClientGamePacketListener> packet, ChannelFutureListener listener, CallbackInfo ci) {
        PacketPatcher.sendExtra((ServerCommonPacketListenerImpl) (Object) this, packet);
    }
}
