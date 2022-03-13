package eu.pb4.polymer.mixin.entity;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.impl.interfaces.MetaConsumer;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"removal"})
@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Shadow @Final private Consumer<Packet<?>> receiver;

    @ModifyVariable(method = "sendPackets", at = @At("HEAD"))
    private Consumer<Packet<?>> polymer_packetWrap(Consumer<Packet<?>> packetConsumer) {
        return (packet) -> packetConsumer.accept(EntityAttachedPacket.set(packet, this.entity));
    }

    /*@ModifyVariable(method = "sendPackets", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/attribute/AttributeContainer;getAttributesToSend()Ljava/util/Collection;"))
    private Collection<EntityAttributeInstance> polymer_sendAttributesOnlyForLivingVirtual(Collection<EntityAttributeInstance> attributes) {
        if (this.entity instanceof PolymerEntity entity && !InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType())) {
            return Collections.emptyList();
        }
        return attributes;
    }*/

    @Inject(method = "sendPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;createSpawnPacket()Lnet/minecraft/network/Packet;"))
    private void polymer_sendPacketsBeforeSpawning(Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (this.entity instanceof PolymerEntity virtualEntity) {
            try {
                virtualEntity.onBeforeSpawnPacket(sender);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "startTracking", at = @At("TAIL"))
    private void polymer_sendEntityInfo(ServerPlayerEntity player, CallbackInfo ci) {
        if (this.entity instanceof PolymerEntity polymerEntity && polymerEntity.shouldSyncWithPolymerClient(player)) {
            PolymerServerProtocol.sendEntityInfo(player.networkHandler, this.entity);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymer_tick(CallbackInfo ci) {
        if (this.entity instanceof PolymerEntity polymerEntity && this.receiver instanceof MetaConsumer receiver) {
            polymerEntity.onEntityTrackerTick(Collections.unmodifiableSet((Set<EntityTrackingListener>) receiver.getAttached()));
        }
    }

    @Inject(method = "sendPackets", at = @At("TAIL"))
    private void polymer_modifyCreationData(Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (this.entity instanceof PolymerEntity) {
            try {
                if (this.entity instanceof LivingEntity livingEntity) {
                    var list = new ArrayList<Pair<EquipmentSlot, ItemStack>>();

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack stack = livingEntity.getEquippedStack(slot);
                        if (!stack.isEmpty()) {
                            list.add(new Pair<>(slot, stack));
                        }
                    }

                    sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), list));
                } else {
                    sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), new ArrayList<>()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
