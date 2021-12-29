package eu.pb4.polymertest;

import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TestHelmetItem extends ArmorItem implements PolymerItem {
    public TestHelmetItem(Settings settings) {
        super(ArmorMaterials.IRON, EquipmentSlot.HEAD, settings);
    }

    @Override
    public Item getPolymerItem(ItemStack stack, ServerPlayerEntity player) {
        return Items.WHITE_STAINED_GLASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.sendMessage(new LiteralText("Use!" + hand), false);
        return super.use(world, user, hand);
    }
}
