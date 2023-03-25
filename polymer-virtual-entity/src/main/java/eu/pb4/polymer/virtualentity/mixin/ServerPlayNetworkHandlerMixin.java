package eu.pb4.polymer.virtualentity.mixin;


import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import eu.pb4.polymer.virtualentity.impl.PacketInterHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @ModifyVariable(method = "onPlayerInteractEntity", at = @At(value = "STORE", ordinal = 0))
    private Entity polymerVE$onInteract(Entity entity, PlayerInteractEntityC2SPacket packet) {
        if (entity == null) {
            var id = ((PlayerInteractEntityC2SPacketAccessor) packet).getEntityId();
            for (var x : this.polymerVE$holders) {
                if (x.isPartOf(id)) {
                    var i = x.getInteraction(id, this.player);
                    if (i != null) {
                        packet.handle(new PacketInterHandler(this.player, i));
                        break;
                    }
                }
            }
        }
        return entity;
    }
}
