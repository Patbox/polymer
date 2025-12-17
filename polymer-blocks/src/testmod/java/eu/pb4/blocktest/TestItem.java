package eu.pb4.blocktest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestItem extends BlockItem implements PolymerItem {
    private final Identifier polymerModel;
    public TestItem(Properties settings, Block block, String modelId) {
        super(block, settings);
        this.polymerModel = ResourcePackExtras.bridgeModel(Identifier.fromNamespaceAndPath("blocktest", modelId));

    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext player) {
        return Items.BARRIER;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.polymerModel;
    }
}
