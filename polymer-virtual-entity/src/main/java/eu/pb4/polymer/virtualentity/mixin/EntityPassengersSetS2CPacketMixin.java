package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.impl.EntityExt;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(EntityPassengersSetS2CPacket.class)
public class EntityPassengersSetS2CPacketMixin {
    @Shadow @Mutable
    private int[] passengerIds;

    @Inject(method = "<init>(Lnet/minecraft/entity/Entity;)V", at = @At(value = "TAIL"))
    private void polymerVE$addExtraPassangers(Entity entity, CallbackInfo ci) {
        var virt = ((EntityExt) entity).polymerVE$getVirtualRidden();
        if (!virt.isEmpty()) {
            var old = this.passengerIds;
            this.passengerIds = Arrays.copyOf(this.passengerIds, old.length + virt.size());
            for (int i = 0; i < virt.size(); i++) {
                this.passengerIds[i + old.length] = virt.getInt(i);
            }
        }
    }
}
