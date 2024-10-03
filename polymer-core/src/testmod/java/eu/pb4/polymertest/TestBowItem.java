package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;


public class TestBowItem extends BowItem implements PolymerItem {
    private final PolymerModelData model;

    public TestBowItem(Settings settings, String model) {
        super(settings);

        this.model = PolymerResourcePackUtils.requestModel(Items.BOW, Identifier.of("polymertest", "item/" + model));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.model.item();
    }
}
