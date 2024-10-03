package eu.pb4.blocktest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestItem extends BlockItem implements PolymerItem {

    private final PolymerModelData polymerModel;

    public TestItem(Settings settings, Block block, String modelId) {
        super(block, settings);
        this.polymerModel = PolymerResourcePackUtils.requestModel(Items.BARRIER, Identifier.of("blocktest", modelId));

    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext player) {
        return this.polymerModel.item();
    }
}
