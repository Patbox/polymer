package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(ExplosionS2CPacket.class)
public class ExplosionS2CPacketMixin {
    @ModifyArg(method = "<init>(DDDFLjava/util/List;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/explosion/Explosion$DestructionType;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/registry/entry/RegistryEntry;)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;"))
    private Iterable<?> polymerCore$removeBlocks(Iterable<?> elements) {
        return PolymerBlockUtils.isStrictBlockUpdateRequired() ? List.of() : elements;
    }
}
