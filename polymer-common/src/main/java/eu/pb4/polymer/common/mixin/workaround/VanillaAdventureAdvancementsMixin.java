package eu.pb4.polymer.common.mixin.workaround;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.core.Holder;
import net.minecraft.data.advancements.packs.VanillaAdventureAdvancements;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(VanillaAdventureAdvancements.class)
public class VanillaAdventureAdvancementsMixin {
    @ModifyExpressionValue(method = "validateMobsToKill", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/worldgen/BootstrapContext;listContextElements(Lnet/minecraft/resources/ResourceKey;)Ljava/util/stream/Stream;"))
    private Stream<Holder.Reference<EntityType<?>>> onlyCheckVanillaEntities(Stream<Holder.Reference<EntityType<?>>> entities) {
        if (CommonImpl.SKIP_REGISTRY_VALIDATION.isBound()) {
            return Stream.empty();
        }

        return entities;
    }
}