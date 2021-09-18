package eu.pb4.polymer.mixin.entity;

import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.entity.VirtualEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Inject(method = "setEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getChunkManager()Lnet/minecraft/server/world/ServerChunkManager;"), cancellable = true)
    private void polymer_sendVirtualInventory(Map<EquipmentSlot, ItemStack> map, CallbackInfo ci) {
        if (this instanceof VirtualEntity) {
            List<Pair<EquipmentSlot, ItemStack>> list = ((VirtualEntity) this).getVirtualEntityEquipment(map);
            if (!list.isEmpty()) {
                ((ServerWorld) this.world).getChunkManager().sendToOtherNearbyPlayers(this, new EntityEquipmentUpdateS2CPacket(this.getId(), list));
            }

            ci.cancel();
        }
    }


    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
}
