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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.Leashable;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @WrapOperation(method = "applyElasticity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocityInternal(Lnet/minecraft/util/math/Vec3d;)V"))
    private void handleVelocity(Entity instance, Vec3d velocity, Operation<Void> original) {
        original.call(instance, velocity);
        if (instance instanceof ServerPlayerEntity player) {
            player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(0,
                    new EntityPosition(Vec3d.ZERO, velocity, 0, 0),
                    Set.of(PositionFlag.DELTA_X, PositionFlag.DELTA_Y, PositionFlag.DELTA_Z, PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT)
            ));
        }
    }

    @ModifyArg(method = "attachLeash(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;sendToOtherNearbyPlayers(Lnet/minecraft/entity/Entity;Lnet/minecraft/network/packet/Packet;)V"))
    private static Packet<?> modifyInitialPacket(Packet<?> packet, @Local(argsOnly = true, ordinal = 0) Entity entity, @Local(argsOnly = true, ordinal = 1) Entity holdingEntity) {
        if (entity instanceof ServerPlayerEntity player) {
            var element = UniqueIdentifiableAttachment.get(player, LeadAttachmentElement.LEAD);

            if (element == null) {
                {
                    var holder = new ElementHolder();
                    var attach = new LeadAttachmentElement(0.5f);
                    attach.setOffset(new Vec3d(0, 0.6f, 0));
                    holder.addElement(attach);
                    element = IdentifiedUniqueEntityAttachment.ofTicking(LeadAttachmentElement.LEAD, holder, player);
                }
                {
                    var holder = new ElementHolder() {
                        @Override
                        public boolean startWatching(ServerPlayNetworkHandler net) {
                            if (net.player != player) {
                                return false;
                            }

                            return super.startWatching(net);
                        }
                    };
                    var attach = new LeadAttachmentElement(0.25f);
                    attach.setOffset(new Vec3d(0, 0.6f, 0));
                    attach.ignorePositionUpdates();
                    var positioner = new ItemDisplayElement();
                    positioner.setInvisible(true);
                    positioner.setTeleportDuration(1);
                    positioner.setOffset(new Vec3d(0, 0.6f, 0));
                    holder.addElement(attach);
                    holder.addElement(positioner);

                    IdentifiedUniqueEntityAttachment.ofTicking(LeadAttachmentElement.LEAD_SELF, holder, player).startWatching(player);

                    player.networkHandler.sendPacket(VirtualEntityUtils.createRidePacket(positioner.getEntityId(), IntList.of(attach.getEntityId())));
                    player.networkHandler.sendPacket(VirtualEntityUtils.createEntityAttachPacket(attach.getEntityId(), holdingEntity.getId()));
                }
            }

            packet = VirtualEntityUtils.createEntityAttachPacket(element.holder().getEntityIds().getInt(0), holdingEntity.getId());
        }

        return packet;
    }

    @Inject(method = "detachLeash(Lnet/minecraft/entity/Entity;ZZ)V", at = @At("HEAD"))
    private static <E extends Entity & Leashable> void modifyInitialPacket(E entity, boolean sendPacket, boolean dropItem, CallbackInfo ci) {
        if (entity instanceof ServerPlayerEntity player) {
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
