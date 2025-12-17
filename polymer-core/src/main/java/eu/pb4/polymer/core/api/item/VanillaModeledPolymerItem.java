package eu.pb4.polymer.core.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.packettweaker.PacketContext;

public interface VanillaModeledPolymerItem extends PolymerItem {
    @Override
    default Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }
}
