package eu.pb4.polymer.mixin.client.item;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Environment(EnvType.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {

    @Shadow protected abstract void search();

    @Shadow private TextFieldWidget searchBox;

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }


    @Inject(method = "search", at = @At("TAIL"))
    private void polymer_hideServerPolymerItems(CallbackInfo ci) {
        if (this.searchBox.getText().isEmpty()) {
            this.handler.itemList.removeIf((i) -> PolymerItemUtils.isPolymerServerItem(i));

            for (var group : ItemGroups.getGroups()) {
                if (group.getType() != ItemGroup.Type.CATEGORY) {
                    continue;
                }

                Collection<ItemStack> stacks;
//todo
                /*if (group instanceof InternalClientItemGroup clientItemGroup) {
                    stacks = clientItemGroup.getStacks();
                } else {*/
                    stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
                //}

                if (stacks != null) {
                    for (var stack : stacks) {
                        this.handler.itemList.add(stack);
                    }
                }
            }
        }
    }
}
