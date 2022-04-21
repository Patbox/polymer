package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.other.PolymerStatusEffect;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.StatusEffectPacketExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RemoveEntityStatusEffectS2CPacket.class)
public class RemoveEntityStatusEffectS2CPacketMixin implements StatusEffectPacketExtension {

    @Mutable @Shadow @Final private StatusEffect effectType;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;)V"))
    private Object polymer_onReplaceEffect(Object obj) {
        if (obj instanceof PolymerStatusEffect virtualEffect) {
            var out = virtualEffect.getPolymerStatusEffect(PolymerUtils.getPlayer());

            if (out != null) {
                return out;
            } else {
                return StatusEffects.UNLUCK;
            }
        }
        return obj;
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;"))
    private Object polymer_remapEffect(PacketByteBuf instance, IndexedIterable<?> registry) {
        return InternalClientRegistry.decodeStatusEffect(instance.readVarInt());
    }

    @Override
    public StatusEffect polymer_getStatusEffect() {
        return this.effectType;
    }
}
