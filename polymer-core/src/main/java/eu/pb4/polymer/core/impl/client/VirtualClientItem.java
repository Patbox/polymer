package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.client.ClientPolymerItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public class VirtualClientItem extends Item {
    private ClientPolymerItem polymerItem;

    public static VirtualClientItem of(ClientPolymerItem item) {
        var obj = CommonImplUtils.createUnsafe(VirtualClientItem.class);
        obj.polymerItem = item;
        return obj;
    }

    @Override
    public RegistryEntry.Reference<Item> getRegistryEntry() {
        return this.polymerItem.visualStack().getItem().getRegistryEntry();
    }

    @Override
    public ItemStack getDefaultStack() {
        return this.polymerItem.visualStack().copy();
    }
    @Override
    public Text getName(ItemStack stack) {
        return this.polymerItem.visualStack().getName();
    }

    public ClientPolymerItem getPolymerEntry() {
        return this.polymerItem;
    }

    @Override
    public ComponentMap getComponents() {
        return this.polymerItem.visualStack().getComponents();
    }

    @Override
    public int getMaxCount() {
        return this.polymerItem.visualStack().getMaxCount();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (this.polymerItem.visualStack().contains(DataComponentTypes.LORE)) {
            tooltip.addAll(this.polymerItem.visualStack().get(DataComponentTypes.LORE).lines());
        }
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return FeatureSet.empty();
    }

    private VirtualClientItem() {
        super(null);
    }
}
