package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.other.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.other.ScheduledPacket;
import eu.pb4.polymer.resourcepack.ResourcePackUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PolymerNetworkHandlerExtension {
    @Shadow private int ticks;

    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Unique
    private boolean polymer_hasResourcePack = false;
    private ArrayList<ScheduledPacket> polymer_scheduledPackets = new ArrayList<>();

    @Override
    public boolean polymer_hasResourcePack() {
        return this.polymer_hasResourcePack;
    }

    @Override
    public void polymer_setResourcePack(boolean value) {
        this.polymer_hasResourcePack = value;
    }

    @Override
    public void polymer_schedulePacket(Packet<?> packet, int duration) {
        this.polymer_scheduledPackets.add(new ScheduledPacket(packet, this.ticks + duration));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void polymer_sendScheduledPackets(CallbackInfo ci) {
        if (!this.polymer_scheduledPackets.isEmpty()) {
            var array = this.polymer_scheduledPackets;
            this.polymer_scheduledPackets = new ArrayList<>();

            for (var entry : array) {
                if (entry.time() <= this.ticks) {
                    this.sendPacket(entry.packet());
                } else {
                    this.polymer_scheduledPackets.add(entry);
                }
            }
        }
    }

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    private void polymer_changeStatus(ResourcePackStatusC2SPacket packet, CallbackInfo ci) {
        if (ResourcePackUtils.shouldCheckByDefault()) {
            this.polymer_hasResourcePack = switch (packet.getStatus()) {
                case ACCEPTED, SUCCESSFULLY_LOADED -> true;
                case DECLINED, FAILED_DOWNLOAD -> false;
            };
        }
    }
}
