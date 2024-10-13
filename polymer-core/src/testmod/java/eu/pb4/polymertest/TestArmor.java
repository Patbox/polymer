package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestArmor extends ArmorItem implements VanillaModeledPolymerItem {
    private final Item itemDefault;

    public TestArmor(EquipmentSlot slot, Identifier model, Settings settings) {
        super(ArmorMaterials.DIAMOND, switch (slot) {
            case HEAD -> EquipmentType.HELMET;
            case CHEST -> EquipmentType.CHESTPLATE;
            case LEGS -> EquipmentType.LEGGINGS;
            default -> EquipmentType.BOOTS;
        }, settings.maxDamage(10000));
        this.itemDefault = getItemFor(slot, false);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.itemDefault;
    }


    private static Item getItemFor(EquipmentSlot slot, boolean bool) {
        if (bool) {
            return switch (slot) {
                case HEAD -> Items.LEATHER_HELMET;
                case CHEST -> Items.LEATHER_CHESTPLATE;
                case LEGS -> Items.LEATHER_LEGGINGS;
                case FEET -> Items.LEATHER_BOOTS;
                default -> Items.STONE;
            };
        } else {
            return switch (slot) {
                case HEAD -> Items.IRON_HELMET;
                case CHEST -> Items.IRON_CHESTPLATE;
                case LEGS -> Items.IRON_LEGGINGS;
                case FEET -> Items.IRON_BOOTS;
                default -> Items.STONE;
            };
        }
    }
}
