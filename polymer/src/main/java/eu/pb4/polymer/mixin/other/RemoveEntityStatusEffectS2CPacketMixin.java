package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerStatusEffect;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.StatusEffectPacketExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RemoveEntityStatusEffectS2CPacket.class)
public class RemoveEntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Mutable @Shadow @Final private StatusEffect effectType;

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;getRawId(Lnet/minecraft/entity/effect/StatusEffect;)I"))
    private int polymer_onReplaceEffect(StatusEffect effect) {
        if (effect instanceof PolymerStatusEffect virtualEffect) {
            var out = virtualEffect.getPolymerStatusEffect(PolymerUtils.getPlayer());

            if (out != null) {
                return StatusEffect.getRawId(out);
            } else {
                return 0;
            }
        }
        return StatusEffect.getRawId(effect);
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/effect/StatusEffect;byRawId(I)Lnet/minecraft/entity/effect/StatusEffect;"))
    private StatusEffect polymer_remapEffect(int rawId) {
        return InternalClientRegistry.decodeStatusEffect(rawId);
    }

    @Override
    public StatusEffect polymer_getStatusEffect() {
        return this.effectType;
    }
}
