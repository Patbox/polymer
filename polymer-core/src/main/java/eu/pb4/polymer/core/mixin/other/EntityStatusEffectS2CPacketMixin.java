package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityStatusEffectS2CPacket.class)
public class EntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Shadow
    @Final
    private StatusEffect effectId;

    @Override
    public StatusEffect polymer$getStatusEffect() {
        return this.effectId;
    }
}
