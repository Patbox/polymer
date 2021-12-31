package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleS2CPacket.class)
public class ParticleS2CPacketMixin {
    @Shadow @Final private ParticleEffect parameters;

    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/particle/ParticleEffect;write(Lnet/minecraft/network/PacketByteBuf;)V"))
    private void polymer_modifyParticle(ParticleEffect instance, PacketByteBuf buf) {
        polymer_remap(instance).write(buf);
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getParameters", at = @At("HEAD"), cancellable = true)
    private void polymer_modifyClient(CallbackInfoReturnable<ParticleEffect> cir) {
        cir.setReturnValue(polymer_remap(this.parameters));
    }


    @Unique
    private ParticleEffect polymer_remap(ParticleEffect instance) {
        if (instance instanceof BlockStateParticleEffect particleEffect) {
            return new BlockStateParticleEffect(
                    (ParticleType<BlockStateParticleEffect>) instance.getType(),
                    PolymerBlockUtils.getPolymerBlockState(particleEffect.getBlockState(), PolymerUtils.getPlayer())
            );
        } else if (instance instanceof ItemStackParticleEffect particleEffect) {
            return new ItemStackParticleEffect(
                    (ParticleType<ItemStackParticleEffect>) instance.getType(),
                    PolymerItemUtils.getPolymerItemStack(particleEffect.getItemStack(), PolymerUtils.getPlayer())
            );
        }
        return instance;
    }
}
