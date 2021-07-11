package eu.pb4.polymertest;

import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TestHelmetItem extends ArmorItem implements VirtualItem {
    public TestHelmetItem(Settings settings) {
        super(ArmorMaterials.IRON, EquipmentSlot.HEAD, settings);
    }

    @Override
    public Item getVirtualItem() {
        return Items.WHITE_STAINED_GLASS;
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.sendMessage(new LiteralText("Use!" + hand), false);
        return super.use(world, user, hand);
    }
}
