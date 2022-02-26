package eu.pb4.polymer.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.item.MiningToolItem;
import net.minecraft.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MiningToolItem.class)
public interface MiningToolItemAccessor {
    @Accessor
    TagKey<Block> getEffectiveBlocks();
}
