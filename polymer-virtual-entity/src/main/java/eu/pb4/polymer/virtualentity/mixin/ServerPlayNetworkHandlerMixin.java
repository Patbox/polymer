package eu.pb4.polymer.virtualentity.mixin;


import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin implements HolderHolder {
    @Unique
    private final Collection<ElementHolder> polymerVE$holders = new ArrayList<>();
    @Shadow
    public ServerPlayer player;

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
    @ModifyVariable(method = "handlePickItemFromEntity", at = @At(value = "STORE", ordinal = 0))
    private Entity polymerVE$onPickEntity(Entity entity, ServerboundPickItemFromEntityPacket packet) {
        if (entity == null && !this.polymerVE$holders.isEmpty()) {
            for (var x : this.polymerVE$holders) {
                if (x.isPartOf(packet.id())) {
                    var i = x.getInteraction(packet.id(), this.player);
                    if (i != null) {
                        i.pickItem(this.player, packet.includeData());
                        break;
                    }
                }
            }
        }
        return entity;
    }


    @ModifyVariable(method = "handleInteract", at = @At(value = "STORE", ordinal = 0))
    private Entity polymerVE$onInteract(Entity entity, ServerboundInteractPacket packet) {
        if (entity == null && !this.polymerVE$holders.isEmpty()) {
            var id = packet.entityId();
            for (var x : this.polymerVE$holders) {
                if (x.isPartOf(id)) {
                    var i = x.getInteraction(id, this.player);
                    if (i != null) {
                        i.interact(this.player, packet.hand(), packet.location(), packet.usingSecondaryAction());
                        break;
                    }
                }
            }
        }
        return entity;
    }

    @ModifyVariable(method = "handleAttack", at = @At(value = "STORE", ordinal = 0))
    private Entity polymerVE$onAttack(Entity entity, ServerboundAttackPacket packet) {
        if (entity == null && !this.polymerVE$holders.isEmpty()) {
            var id = packet.entityId();
            for (var x : this.polymerVE$holders) {
                if (x.isPartOf(id)) {
                    var i = x.getInteraction(id, this.player);
                    if (i != null) {
                        i.attack(this.player);
                        break;
                    }
                }
            }
        }
        return entity;
    }
}
