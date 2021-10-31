package eu.pb4.polymer.impl.interfaces;

import net.minecraft.item.ItemStack;

import java.util.List;

public interface ClientItemGroupExtension {
    void polymer_addStacks(List<ItemStack> stackList);
    void polymer_addStack(ItemStack stack);
    void polymer_clearStacks();
}
