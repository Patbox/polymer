package eu.pb4.polymer.impl.client.interfaces;

import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

public interface ClientItemGroupExtension {
    void polymer_addStacks(List<ItemStack> stackList);
    void polymer_addStack(ItemStack stack);
    void polymer_clearStacks();
    Collection<ItemStack> polymer_getStacks();
    void polymer_removeStacks(Collection<ItemStack> stacks);
}
