package eu.pb4.polymer.core.impl.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
@SuppressWarnings({"unused"})
public interface ClientItemGroupExtension {
    void polymer$addStackGroup(ItemStack stack);
    void polymer$addStackSearch(ItemStack stack);
    void polymer$clearStacks();
    Collection<ItemStack> polymer$getStacksGroup();
    Collection<ItemStack> polymer$getStacksSearch();

    void polymerCore$setPos(ItemGroup.Row row, int slot);
    void polymerCore$setPage(int page);
    int polymerCore$getPage();
}
