package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.client.ClientPolymerItem;
import eu.pb4.polymer.core.mixin.item.ItemAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public class VirtualClientItem extends Item {
    private ClientPolymerItem polymerItem;

    public static VirtualClientItem of(ClientPolymerItem item) {
        var obj = CommonImplUtils.createUnsafe(VirtualClientItem.class);
        obj.polymerItem = item;
        ((ItemAccessor) obj).setRequiredFeatures(FeatureFlagSet.of());
        return obj;
    }

    @Override
    public Holder.Reference<Item> builtInRegistryHolder() {
        return this.polymerItem.visualStack().getItem().builtInRegistryHolder();
    }

    @Override
    public ItemStack getDefaultInstance() {
        return this.polymerItem.visualStack().copy();
    }
    @Override
    public Component getName(ItemStack stack) {
        return this.polymerItem.visualStack().getHoverName();
    }

    public ClientPolymerItem getPolymerEntry() {
        return this.polymerItem;
    }

    @Override
    public DataComponentMap components() {
        return this.polymerItem.visualStack().getComponents();
    }

    @Override
    public int getDefaultMaxStackSize() {
        return this.polymerItem.visualStack().getMaxStackSize();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        if (this.polymerItem.visualStack().has(DataComponents.LORE)) {
            this.polymerItem.visualStack().getOrDefault(DataComponents.LORE, ItemLore.EMPTY).addToTooltip(context, textConsumer, type, stack.getComponents());
        }
    }

    private VirtualClientItem() {
        super(null);
    }
}
