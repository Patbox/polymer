package eu.pb4.polymertest;

import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;

public class TestHelmetItem extends ArmorItem implements VirtualItem {
    public TestHelmetItem(Settings settings) {
        super(ArmorMaterials.IRON, EquipmentSlot.HEAD, settings);
    }

    @Override
    public Item getVirtualItem() {
        return Items.WHITE_STAINED_GLASS;
    }
}
