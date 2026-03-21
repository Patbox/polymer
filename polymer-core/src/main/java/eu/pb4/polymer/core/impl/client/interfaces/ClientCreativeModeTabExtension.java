package eu.pb4.polymer.core.impl.client.interfaces;

import eu.pb4.polymer.core.impl.networking.payloads.s2c.PolymerCreativeTabContentAddS2CPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
@SuppressWarnings({"unused"})
public interface ClientCreativeModeTabExtension {
    void polymer$handleEntries(List<PolymerCreativeTabContentAddS2CPayload.Entry> main, List<PolymerCreativeTabContentAddS2CPayload.Entry> search);
    void polymer$clearStacks();
    Collection<ItemStack> polymer$getStacksGroup();
    Collection<ItemStack> polymer$getStacksSearch();

    void polymerCore$setPos(CreativeModeTab.Row row, int slot);
    void polymerCore$setPage(int page);
    int polymerCore$getPage();
}
