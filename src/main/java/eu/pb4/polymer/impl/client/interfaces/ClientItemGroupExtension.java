package eu.pb4.polymer.impl.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public interface ClientItemGroupExtension {
    void polymer_addStacks(List<ItemStack> stackList);
    void polymer_addStack(ItemStack stack);
    void polymer_clearStacks();
    Collection<ItemStack> polymer_getStacks();
    void polymer_removeStacks(Collection<ItemStack> stacks);
}
