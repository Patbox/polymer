package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.ItemStack;

public class SulfurCubeModelTestFix extends ElementHolder {
    private final SulfurCube entity;
    private final ItemDisplayElement item;

    public SulfurCubeModelTestFix(SulfurCube self) {
        super();
        this.entity = self;
        this.item = new ItemDisplayElement();
        this.item.setSendPositionUpdates(false);
        this.addPassengerElement(this.item);
    }

    @Override
    protected void onTick() {
        super.onTick();
        var stack = entity.getItemBySlot(EquipmentSlot.BODY);
        if (!(stack.getItem() instanceof PolymerItem polymerItem)) {
            if (!this.item.getItem().isEmpty()) {
                this.item.setItem(ItemStack.EMPTY);
            }
            return;
        }

        if (ItemStack.isSameItemSameComponents(this.item.getItem(), stack)) {
            return;
        }

        this.item.setItem(stack.copy());
    }
}
