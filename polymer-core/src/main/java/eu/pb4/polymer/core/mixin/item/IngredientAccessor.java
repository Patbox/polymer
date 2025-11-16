package eu.pb4.polymer.core.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
    @Accessor
    RegistryEntryList<Item> getEntries();

    @Accessor
    @Mutable
    void setEntries(RegistryEntryList<Item> entries);
}
