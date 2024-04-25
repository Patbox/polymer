package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TestHelmetItem extends ArmorItem implements PolymerItem {
    public TestHelmetItem(Settings settings) {
        super(ArmorMaterials.IRON, Type.HELMET, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, ServerPlayerEntity player) {
        return Items.WHITE_STAINED_GLASS;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, @Nullable ServerPlayerEntity player) {
        var x = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, player);
        x.set(DataComponentTypes.RARITY, Rarity.EPIC);
        return x;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.sendMessage(Text.literal("Use!" + hand), false);
        return super.use(world, user, hand);
    }
}
