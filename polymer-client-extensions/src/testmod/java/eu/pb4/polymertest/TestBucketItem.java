package eu.pb4.polymertest;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class TestBucketItem extends BucketItem implements PolymerItem {
    private final Item polymerItem;

    public TestBucketItem(Fluid fluid, Settings settings, Item polymerItem) {
        super(fluid, settings);
        this.polymerItem = polymerItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.polymerItem;
    }
}
