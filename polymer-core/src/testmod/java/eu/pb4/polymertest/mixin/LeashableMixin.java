package eu.pb4.polymertest.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.IdentifiedUniqueEntityAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.UniqueIdentifiableAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymertest.LeadAttachmentElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @WrapOperation(method = "checkElasticInteractions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void handleVelocity(Entity instance, Vec3 velocity, Operation<Void> original) {
        original.call(instance, velocity);
        if (instance instanceof ServerPlayer player) {
            player.connection.send(new ClientboundPlayerPositionPacket(0,
                    new PositionMoveRotation(Vec3.ZERO, velocity, 0, 0),
                    Set.of(Relative.DELTA_X, Relative.DELTA_Y, Relative.DELTA_Z, Relative.X, Relative.Y, Relative.Z, Relative.X_ROT, Relative.Y_ROT)
            ));
        }
    }

    @ModifyArg(method = "setLeashedTo(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;sendToTrackingPlayers(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/network/protocol/Packet;)V"))
    private static Packet<?> modifyInitialPacket(Packet<?> packet, @Local(argsOnly = true, ordinal = 0) Entity entity, @Local(argsOnly = true, ordinal = 1) Entity holdingEntity) {
        if (entity instanceof ServerPlayer player) {
            var element = UniqueIdentifiableAttachment.get(player, LeadAttachmentElement.LEAD);

            if (element == null) {
                {
                    var holder = new ElementHolder();
                    var attach = new LeadAttachmentElement(0.5f);
                    attach.setOffset(new Vec3(0, 0.6f, 0));
                    holder.addElement(attach);
                    element = IdentifiedUniqueEntityAttachment.ofTicking(LeadAttachmentElement.LEAD, holder, player);
                }
                {
                    var holder = new ElementHolder() {
                        @Override
                        public boolean startWatching(ServerGamePacketListenerImpl net) {
                            if (net.player != player) {
                                return false;
                            }

                            return super.startWatching(net);
                        }
                    };
                    var attach = new LeadAttachmentElement(0.25f);
                    attach.setOffset(new Vec3(0, 0.6f, 0));
                    attach.ignorePositionUpdates();
                    var positioner = new ItemDisplayElement();
                    positioner.setInvisible(true);
                    positioner.setTeleportDuration(1);
                    positioner.setOffset(new Vec3(0, 0.6f, 0));
                    holder.addElement(attach);
                    holder.addElement(positioner);

                    IdentifiedUniqueEntityAttachment.ofTicking(LeadAttachmentElement.LEAD_SELF, holder, player).startWatching(player);

                    player.connection.send(VirtualEntityUtils.createRidePacket(positioner.getEntityId(), IntList.of(attach.getEntityId())));
                    player.connection.send(VirtualEntityUtils.createEntityAttachPacket(attach.getEntityId(), holdingEntity.getId()));
                }
            }

            packet = VirtualEntityUtils.createEntityAttachPacket(element.holder().getEntityIds().getInt(0), holdingEntity.getId());
        }

        return packet;
    }

    @Inject(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("HEAD"))
    private static <E extends Entity & Leashable> void modifyInitialPacket(E entity, boolean sendPacket, boolean dropItem, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            var element = UniqueIdentifiableAttachment.get(player, LeadAttachmentElement.LEAD);
            if (element != null) {
                element.destroy();
            }
            element = UniqueIdentifiableAttachment.get(player, LeadAttachmentElement.LEAD_SELF);
            if (element != null) {
                element.destroy();
            }
        }
    }
}
