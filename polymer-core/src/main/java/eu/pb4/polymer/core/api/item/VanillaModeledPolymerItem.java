package eu.pb4.polymer.core.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public interface VanillaModeledPolymerItem extends PolymerItem {
    @Override
    default Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }
}
