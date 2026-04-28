package eu.pb4.polymer.core.mixin.item;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
    @Accessor("values")
    HolderSet<Item> getEntries();

    @Accessor("values")
    @Mutable
    void setEntries(HolderSet<Item> entries);
}
