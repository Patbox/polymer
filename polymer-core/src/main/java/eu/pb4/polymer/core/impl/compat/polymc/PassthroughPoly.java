package eu.pb4.polymer.core.impl.compat.polymc;

import io.github.theepicblock.polymc.api.block.BlockPoly;
import io.github.theepicblock.polymc.api.entity.EntityPoly;
import io.github.theepicblock.polymc.api.item.ItemPoly;
import net.minecraft.entity.Entity;


public interface PassthroughPoly {
    ItemPoly ITEM = (ItemPoly & PassthroughPoly) (itemStack, serverPlayerEntity, itemLocation) -> itemStack.copy();
    BlockPoly BLOCK = (BlockPoly & PassthroughPoly) blockState -> blockState;
    EntityPoly<?> ENTITY = (EntityPoly<?> & PassthroughPoly) (wizardInfo, entity) -> null;

    static <T extends Entity> EntityPoly<T> entity() {
        //noinspection unchecked
        return (EntityPoly<T>) ENTITY;
    }
}
