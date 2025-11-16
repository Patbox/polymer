package eu.pb4.polymer.core.mixin.client.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.interfaces.IngredientExtension;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplays;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Ingredient.class)
public class IngredientMixin implements IngredientExtension {
    @Unique
    private Set<Identifier> polymerItemIds = Set.of();

    @Unique
    private SlotDisplay polymerItems = null;

    @Override
    public void polymer$setPolymerItems(IntList polymerItems) {
        var set = new HashSet<Identifier>(polymerItems.size());
        var list = new ArrayList<SlotDisplay>(polymerItems.size());
        for (var numId : polymerItems) {
            var item = InternalClientRegistry.ITEMS.get(numId);
            if (item != null) {
                set.add(item.identifier());
                list.add(new SlotDisplay.StackSlotDisplay(item.visualStack()));
            }
        }
        this.polymerItemIds = set;
        this.polymerItems = new SlotDisplay.CompositeSlotDisplay(list);
    }

    @Inject(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void polymericTest(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        var id = PolymerItemUtils.getServerIdentifier(itemStack);
        if (id != null) {
            cir.setReturnValue(this.polymerItemIds.contains(id));
        }
    }

    @ModifyReturnValue(method = "toDisplay()Lnet/minecraft/recipe/display/SlotDisplay;", at = @At("RETURN"))
    private SlotDisplay updateDisplay(SlotDisplay original) {
        return this.polymerItems == null ? original : new SlotDisplay.CompositeSlotDisplay(List.of(original, this.polymerItems));
    }
}
