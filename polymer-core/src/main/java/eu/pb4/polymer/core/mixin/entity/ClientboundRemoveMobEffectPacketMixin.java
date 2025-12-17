package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundRemoveMobEffectPacket.class)
public class ClientboundRemoveMobEffectPacketMixin implements StatusEffectPacketExtension {


    @Shadow @Final private Holder<MobEffect> effect;

    @Override
    public MobEffect polymer$getStatusEffect() {
        return this.effect.value();
    }
}
