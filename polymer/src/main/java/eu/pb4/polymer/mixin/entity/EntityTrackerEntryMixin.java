package eu.pb4.polymer.mixin.entity;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @SuppressWarnings("unchecked")
    @ModifyVariable(method = "sendPackets", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/attribute/AttributeContainer;getAttributesToSend()Ljava/util/Collection;"))
    private Collection<EntityAttributeInstance> polymer_sendAttributesOnlyForLivingVirtual(Collection<EntityAttributeInstance> attributes) {
        if (this.entity instanceof PolymerEntity entity) {
            if (!InternalEntityHelpers.isLivingEntity(entity.getPolymerEntityType())) {
                return List.of();
            }

            DefaultAttributeContainer vanillaContainer = DefaultAttributeRegistry.get((EntityType<? extends LivingEntity>) entity.getPolymerEntityType());
            return attributes.stream().filter(entityAttributeInstance -> vanillaContainer.has(entityAttributeInstance.getAttribute())).toList();
        }

        return attributes;
    }

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

    @Inject(method = "sendPackets", at = @At("TAIL"))
    private void polymer_modifyCreationData(Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (this.entity instanceof PolymerEntity virtualEntity) {
            try {
                if (this.entity instanceof LivingEntity livingEntity) {
                    Map<EquipmentSlot, ItemStack> map = new HashMap<>();

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack stack = livingEntity.getEquippedStack(slot);
                        if (!stack.isEmpty()) {
                            map.put(slot, stack);
                        }
                    }

                    List<Pair<EquipmentSlot, ItemStack>> list = virtualEntity.getPolymerVisibleEquipment(map);
                    if (!list.isEmpty()) {
                        sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), list));
                    }
                } else {
                    List<Pair<EquipmentSlot, ItemStack>> list = virtualEntity.getPolymerVisibleEquipment(Collections.emptyMap());
                    if (!list.isEmpty()) {
                        sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), list));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
