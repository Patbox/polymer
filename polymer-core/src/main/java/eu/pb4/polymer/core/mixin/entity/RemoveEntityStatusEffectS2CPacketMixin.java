package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RemoveEntityStatusEffectS2CPacket.class)
public class RemoveEntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {


    @Shadow @Final private RegistryEntry<StatusEffect> effect;

    @Override
    public StatusEffect polymer$getStatusEffect() {
        return this.effect.value();
    }
}
