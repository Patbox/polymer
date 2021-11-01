package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @ModifyVariable(method = "initializeSearchableContainers", at = @At(value = "STORE", ordinal = 0))
    private DefaultedList<?> polymer_removePolymerItemsFromSearch(DefaultedList<?> og) {
        return new DefaultedList<>(new ArrayList<>(), null) {
            @Override
            public void add(int value, Object element) {
                if (element instanceof ItemStack stack && !PolymerItemUtils.isPolymerServerItem(stack)) {
                    super.add(value, element);
                }
            }
        };
    }

    /*
    @Inject(method = "initializeSearchableContainers", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer_addCustomEntries(CallbackInfo ci, TextSearchableContainer textSearchableContainer, IdentifierSearchableContainer identifierSearchableContainer, DefaultedList<ItemStack> defaultedList) {
        for (var group : ItemGroup.GROUPS) {
            if (group instanceof InternalClientItemGroup clientItemGroup) {
                defaultedList.addAll(clientItemGroup.getStacks());
            } else {
                defaultedList.addAll(((ClientItemGroupExtension) group).polymer_getStacks());
            }
        }
    }*/
}
