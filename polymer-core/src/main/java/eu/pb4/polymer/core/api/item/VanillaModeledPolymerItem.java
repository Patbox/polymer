package eu.pb4.polymer.core.api.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public interface VanillaModeledPolymerItem extends PolymerItem {
    @Override
    default Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return null;
    }
}
