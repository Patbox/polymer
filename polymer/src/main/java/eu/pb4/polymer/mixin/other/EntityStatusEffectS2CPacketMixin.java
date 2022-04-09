package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerStatusEffect;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.StatusEffectPacketExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityStatusEffectS2CPacket.class)
public class EntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Mutable
    @Shadow @Final private StatusEffect effectId;
    @Unique private StatusEffect polymer_effect = null;

    @Inject(method = "<init>(ILnet/minecraft/entity/effect/StatusEffectInstance;)V", at = @At("TAIL"))
    public void polymer_onWrite(int entityId, StatusEffectInstance effect, CallbackInfo ci) {
        this.polymer_effect = effect.getEffectType();
        if (this.effectId instanceof PolymerStatusEffect virtualEffect && virtualEffect.getPolymerStatusEffect() != null) {
            this.polymer_effect = virtualEffect.getPolymerStatusEffect();

            this.effectId = virtualEffect.getPolymerStatusEffect();
        }
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;"))
    private Object polymer_remapEffect(PacketByteBuf instance, IndexedIterable<?> registry) {
        return InternalClientRegistry.decodeStatusEffect(instance.readVarInt());
    }

    @Override
    public StatusEffect polymer_getStatusEffect() {
        return this.polymer_effect;
    }
}
