package eu.pb4.polymer.core.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerTrackerPacketSender;
import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import eu.pb4.polymer.core.impl.interfaces.PossiblyInitialPacket;
import eu.pb4.polymer.core.impl.networking.PolymerServerProtocol;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    @Shadow @Nullable private List<SynchedEntityData.DataValue<?>> trackedDataValues;

    @Shadow @Final private ServerEntity.Synchronizer synchronizer;

    @ModifyVariable(method = "sendPairingData", at = @At("HEAD"), argsOnly = true)
    private Consumer<Packet<?>> polymer$packetWrap(Consumer<Packet<?>> packetConsumer, @Local(argsOnly = true) ServerPlayer player) {
        return PlayerBoundConsumer.createPacketFor(Set.of(player.connection), this.entity, packetConsumer);
    }

    @ModifyArg(method = "sendPairingData", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1))
    private Object polymer$markAsInitial(Object obj) {
        ((PossiblyInitialPacket) obj).polymer$setInitial();
        return obj;
    }

    @ModifyArg(method = "sendPairingData", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 2))
    private Object polymer$markAsInitial2(Object obj) {
        ((PossiblyInitialPacket) obj).polymer$setInitial();
        return obj;
    }

    @Inject(method = "sendPairingData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getAddEntityPacket(Lnet/minecraft/server/level/ServerEntity;)Lnet/minecraft/network/protocol/Packet;"))
    private void polymer$sendPacketsBeforeSpawning(ServerPlayer player, Consumer<Packet<?>> sender, CallbackInfo ci) {
        var polymerEntity = PolymerEntity.get(this.entity);
        if (polymerEntity != null) {
            try {
                polymerEntity.onBeforeSpawnPacket(player, sender);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void polymer$sendEntityInfo(ServerPlayer player, CallbackInfo ci) {
        var polymerEntity = PolymerEntity.get(this.entity);
        if (polymerEntity != null && polymerEntity.canSynchronizeToPolymerClient(player)) {
            PolymerServerProtocol.sendEntityInfo(player.connection, this.entity);
        }
    }

    @Inject(method = "sendChanges", at = @At("HEAD"))
    private void polymer$tickHead(CallbackInfo ci) {
        var polymerEntity = PolymerEntity.get(this.entity);
        if (polymerEntity != null && this.synchronizer instanceof PolymerTrackerPacketSender accessor) {
            polymerEntity.beforeEntityTrackerTick(Collections.unmodifiableSet(accessor.listeners()));
        }
    }

    @Inject(method = "sendChanges", at = @At("TAIL"))
    private void polymer$tick(CallbackInfo ci) {
        var polymerEntity = PolymerEntity.get(this.entity);
        if (polymerEntity != null && this.synchronizer instanceof PolymerTrackerPacketSender accessor) {
            polymerEntity.onEntityTrackerTick(Collections.unmodifiableSet(accessor.listeners()));
        }
    }

    @Inject(method = "sendPairingData", at = @At("TAIL"))
    private void polymer$modifyCreationData(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> sender, CallbackInfo ci) {
        var polymerEntity = PolymerEntity.get(this.entity);
        if (polymerEntity != null) {
            if (polymerEntity.sendEmptyTrackerUpdates(player) && this.trackedDataValues == null) {
                var x = new ClientboundSetEntityDataPacket(this.entity.getId(), List.of());
                ((PossiblyInitialPacket) (Object) x).polymer$setInitial();
                sender.accept(x);
            }

            try {
                if (this.entity instanceof LivingEntity livingEntity) {
                    var list = new ArrayList<Pair<EquipmentSlot, ItemStack>>();

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        ItemStack stack = livingEntity.getItemBySlot(slot);
                        if (!stack.isEmpty()) {
                            list.add(new Pair<>(slot, stack));
                        }
                    }

                    sender.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), list));
                } else {
                    sender.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), new ArrayList<>()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
