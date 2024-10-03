package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.VanillaModeledPolymerItem;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestHelmetItem extends ArmorItem implements VanillaModeledPolymerItem {
    public TestHelmetItem(Settings settings) {
        super(ArmorMaterials.IRON, EquipmentType.HELMET, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.WHITE_STAINED_GLASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context) {
        var x = VanillaModeledPolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
        x.set(DataComponentTypes.RARITY, Rarity.EPIC);
        return x;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.sendMessage(Text.literal("Use!" + hand), false);
        return super.use(world, user, hand);
    }
}
