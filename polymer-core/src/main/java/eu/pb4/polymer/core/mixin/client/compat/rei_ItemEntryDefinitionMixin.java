package eu.pb4.polymer.core.mixin.client.compat;

import eu.pb4.polymer.core.api.client.ClientPolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.plugin.client.entry.ItemEntryDefinition;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Yeah I know, but I wanted quick solution without requiring any changes on your side

@Pseudo
@Mixin(ItemEntryDefinition.class)
public abstract class rei_ItemEntryDefinitionMixin {

    @Inject(method = "equals(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lme/shedaniel/rei/api/common/entry/comparison/ComparisonContext;)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$areEqual(ItemStack o1, ItemStack o2, ComparisonContext context, CallbackInfoReturnable<Boolean> cir) {
        if (!CompatUtils.areSamePolymerType(o1, o2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "wildcard(Lme/shedaniel/rei/api/common/entry/EntryStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$wildcard(EntryStack<ItemStack> entry, ItemStack value, CallbackInfoReturnable<ItemStack> cir) {
        var id1 = PolymerItemUtils.getServerIdentifier(value);
        if (id1 != null) {
            var item = ClientPolymerItem.REGISTRY.get(id1);

            if (item != null) {
                cir.setReturnValue(item.visualStack().copy());
            }
        }
    }

    @Inject(method = "getIdentifier(Lme/shedaniel/rei/api/common/entry/EntryStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/Identifier;", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$getIdentifier(EntryStack<ItemStack> entry, ItemStack value, CallbackInfoReturnable<@Nullable Identifier> cir) {
        var id1 = PolymerItemUtils.getServerIdentifier(value);
        if (id1 != null) {
            cir.setReturnValue(id1);
        }
    }
}
