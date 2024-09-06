package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TestArmor extends ArmorItem implements PolymerItem {
    private final PolymerModelData itemModel;
    private final Item itemDefault;

    public TestArmor(EquipmentSlot slot, Identifier model, Settings settings) {
        super(ArmorMaterials.DIAMOND, switch (slot) {
            case HEAD -> EquipmentType.HELMET;
            case CHEST -> EquipmentType.CHESTPLATE;
            case LEGS -> EquipmentType.LEGGINGS;
            default -> EquipmentType.BOOTS;
        }, settings.maxDamage(10000));
        this.itemDefault = getItemFor(slot, false);
        this.itemModel = PolymerResourcePackUtils.requestModel(getItemFor(slot, true), model);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasMainPack(player) ? this.itemModel.item() : this.itemDefault;
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasMainPack(player) ? this.itemModel.value() : -1;
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
