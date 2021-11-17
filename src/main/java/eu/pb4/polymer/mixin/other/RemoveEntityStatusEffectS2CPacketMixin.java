package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerStatusEffect;
import eu.pb4.polymer.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemoveEntityStatusEffectS2CPacket.class)
public class RemoveEntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Mutable @Shadow @Final private StatusEffect effectType;

    @Inject(method = "<init>(ILnet/minecraft/entity/effect/StatusEffect;)V", at = @At("TAIL"))
    private void polymer_onReplaceEffect(int entityId, StatusEffect effectType, CallbackInfo ci) {
        if (this.effectType instanceof PolymerStatusEffect effect) {
            this.effectType = effect.getPolymerStatusEffect();
        }
    }

    @Override
    public StatusEffect polymer_getStatusEffect() {
        return this.effectType;
    }
}
