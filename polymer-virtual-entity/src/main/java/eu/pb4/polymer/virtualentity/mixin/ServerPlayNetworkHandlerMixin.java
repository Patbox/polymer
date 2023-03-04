package eu.pb4.polymer.virtualentity.mixin;


import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Collection;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements HolderHolder {
    @Unique
    private final Collection<ElementHolder> polymerVE$holders = new ArrayList<>();
    @Shadow
    public ServerPlayerEntity player;

    @Override
    public void polymer$addHolder(ElementHolder holderAttachment) {
        this.polymerVE$holders.add(holderAttachment);
    }

    @Override
    public void polymer$removeHolder(ElementHolder holderAttachment) {
        this.polymerVE$holders.remove(holderAttachment);
    }

    @Override
    public Collection<ElementHolder> polymer$getHolders() {
        return this.polymerVE$holders;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymerVE$tick(CallbackInfo ci) {
        try {
            for (var holder : new ArrayList<>(this.polymerVE$holders)) {
                if (holder.getAttachment() == null) {
                    holder.stopWatching(this.player);
                }
            }
        } catch (Throwable e) {
        }
    }

    @Inject(method = "onPlayerInteractEntity", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymerVE$interactWithHologram(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, ServerWorld world, Entity entity) {
        if (entity == null) {
            var id = ((PlayerInteractEntityC2SPacketAccessor) packet).getEntityId();
            for (var x : this.polymerVE$holders) {
                if (x.isPartOf(id)) {
                    var i = x.getInteraction(id, this.player);
                    if (i != null) {
                        packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
                            @Override
                            public void interact(Hand hand) {
                                i.interact(player, hand);
                            }

                            @Override
                            public void interactAt(Hand hand, Vec3d pos) {
                                i.interactAt(player, hand, pos);
                            }

                            @Override
                            public void attack() {
                                i.attack(player);
                            }
                        });
                    }
                    return;
                }
            }
        }
    }
}
