package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerStatusEffect;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.StatusEffectPacketExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityStatusEffectS2CPacket.class)
public class EntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Mutable @Shadow @Final private int effectId;

    @Unique private StatusEffect polymer_effect = null;

    @Inject(method = "<init>(ILnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At("TAIL"))
    public void polymer_onInit(int entityId, StatusEffectInstance effect, CallbackInfo ci) {
        this.polymer_effect = effect.getEffectType();
    }

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeVarInt(I)Lnet/minecraft/network/PacketByteBuf;", ordinal = 1))
    public int polymer_onWrite(int value) {
        if (Registry.STATUS_EFFECT.get(this.effectId) instanceof PolymerStatusEffect virtualEffect) {
            var effect = virtualEffect.getPolymerStatusEffect(PolymerUtils.getPlayer());

            if (effect != null) {
                return StatusEffect.getRawId(effect);
            } else {
                return 0;
            }
        }
        return value;
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readVarInt()I", ordinal = 1))
    private int polymer_remapEffect(PacketByteBuf instance) {
        return StatusEffect.getRawId(InternalClientRegistry.decodeStatusEffect(instance.readVarInt()));
    }

    @Override
    public StatusEffect polymer_getStatusEffect() {
        return this.polymer_effect;
    }
}
