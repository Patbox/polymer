package eu.pb4.polymer.common.mixin.workaround;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import java.util.Map;

@Mixin(BlockLootSubProvider.class)
public class BlockLootSubProviderMixin {
    @Shadow
    @Final
    private Map<ResourceKey<LootTable>, LootTable.Builder> map;

    @ModifyExpressionValue(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private Iterator<Block> skipForPolymer(Iterator<Block> blocks) {
        if (CommonImpl.SKIP_REGISTRY_VALIDATION.isBound()) {
            return Iterators.filter(blocks, block -> block.getLootTable().map(this.map::containsKey).orElse(false));
        }

        return blocks;
    }
}