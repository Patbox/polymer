package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestArmor extends Item implements VanillaModeledPolymerItem {
    private final Item itemDefault;

    public TestArmor(EquipmentSlot slot, Identifier model, Item.Properties settings) {
        super(settings.humanoidArmor(ArmorMaterials.DIAMOND, switch (slot) {
            case HEAD -> ArmorType.HELMET;
            case CHEST -> ArmorType.CHESTPLATE;
            case LEGS -> ArmorType.LEGGINGS;
            default -> ArmorType.BOOTS;
        }).durability(10000));
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
