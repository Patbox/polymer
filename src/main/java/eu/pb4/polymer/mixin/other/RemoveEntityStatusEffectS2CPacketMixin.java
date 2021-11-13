package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.interfaces.VirtualStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemoveEntityStatusEffectS2CPacket.class)
public class RemoveEntityStatusEffectS2CPacketMixin {

    @Mutable @Shadow @Final private StatusEffect effectType;

    @Inject(method = "write", at = @At("HEAD"))
    public void polymer_onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
        if (effectType instanceof VirtualStatusEffect virtualEffect && virtualEffect.getVirtualStatusEffect() != null) {
            this.effectType = ((VirtualStatusEffect) effectType).getVirtualStatusEffect();
        }
    }
}
