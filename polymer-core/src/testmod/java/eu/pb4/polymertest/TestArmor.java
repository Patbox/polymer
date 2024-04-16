package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class TestArmor extends ArmorItem implements PolymerItem {
    private final PolymerModelData itemModel;
    private final PolymerArmorModel armorModel;
    private final Item itemDefault;
    private final AttributeModifiersComponent modifiers;

    public TestArmor(EquipmentSlot slot, Identifier model, Identifier armor) {
        super(ArmorMaterials.DIAMOND, switch (slot) {
            case HEAD -> Type.HELMET;
            case CHEST -> Type.CHESTPLATE;
            case LEGS -> Type.LEGGINGS;
            default -> Type.BOOTS;
        }, new Settings().fireproof().maxDamage(10000));
        this.itemDefault = getItemFor(slot, false);
        this.itemModel = PolymerResourcePackUtils.requestModel(getItemFor(slot, true), model);
        this.armorModel = PolymerResourcePackUtils.requestArmor(armor);
        this.modifiers = super.getAttributeModifiers().with(EntityAttributes.GENERIC_GRAVITY, new EntityAttributeModifier("aaaaa", 0.8, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), AttributeModifierSlot.forEquipmentSlot(slot));
    }

    @Override
    public AttributeModifiersComponent getAttributeModifiers() {
        return this.modifiers;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasMainPack(player) ? this.itemModel.item() : this.itemDefault;
    }

    @Override
    public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasMainPack(player) ? this.armorModel.color() : -1;
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
