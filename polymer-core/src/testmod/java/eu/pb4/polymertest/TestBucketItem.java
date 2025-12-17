package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestBucketItem extends BucketItem implements VanillaModeledPolymerItem {
    private final Item polymerItem;

    public TestBucketItem(Fluid fluid, Properties settings, Item polymerItem) {
        super(fluid, settings);
        this.polymerItem = polymerItem;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.polymerItem;
    }
}
