package eu.pb4.polymer.common.mixin.workaround;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.core.Holder;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.stream.Stream;

@Mixin(EntityLootSubProvider.class)
public class EntityLootSubProviderMixin {
    @Shadow
    @Final
    private Map<EntityType<?>, Map<ResourceKey<LootTable>, LootTable.Builder>> map;

    @ModifyExpressionValue(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/loot/LootTableSubProvider$Context;listContextElements(Lnet/minecraft/resources/ResourceKey;)Ljava/util/stream/Stream;"))
    private Stream<Holder.Reference<EntityType<?>>> onlyVanillaEntities(Stream<Holder.Reference<EntityType<?>>> entities) {
        if (CommonImpl.SKIP_REGISTRY_VALIDATION.isBound()) {
            return entities.filter(entity -> this.map.containsKey(entity.value()));
        }

        return entities;
    }
}