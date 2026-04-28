package eu.pb4.polymer.core.mixin.client.item.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.interfaces.IngredientExtension;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
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
            var item = InternalClientRegistry.ITEMS.byId(numId);
            if (item != null) {
                set.add(item.identifier());
                list.add(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(item.visualStack())));
            }
        }
        this.polymerItemIds = set;
        this.polymerItems = new SlotDisplay.Composite(list);
    }

    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void polymericTest(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        var id = PolymerItemUtils.getPolymerIdentifier(itemStack);
        if (id != null) {
            cir.setReturnValue(this.polymerItemIds.contains(id));
        }
    }

    @ModifyReturnValue(method = "display", at = @At("RETURN"))
    private SlotDisplay updateDisplay(SlotDisplay original) {
        return this.polymerItems == null ? original : new SlotDisplay.Composite(List.of(original, this.polymerItems));
    }
}
