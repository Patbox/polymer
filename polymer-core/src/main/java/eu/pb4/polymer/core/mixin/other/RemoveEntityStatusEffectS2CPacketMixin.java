package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RemoveEntityStatusEffectS2CPacket.class)
public class RemoveEntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Shadow @Final private StatusEffect effectType;

    @Override
    public StatusEffect polymer$getStatusEffect() {
        return this.effectType;
    }
}
