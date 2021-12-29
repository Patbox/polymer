package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroup.class)
public class ItemGroupMixin {
    private static ItemGroup[] GROUPS_OLD;
    @Mutable
    @Shadow @Final public static ItemGroup[] GROUPS;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemGroup;GROUPS:[Lnet/minecraft/item/ItemGroup;", shift = At.Shift.BEFORE))
    private void polymer_replaceArrayToSkipAdding(int index, String id, CallbackInfo ci) {
        if (((Object) this) instanceof PolymerItemGroup) {
            GROUPS_OLD = GROUPS;
            GROUPS = new ItemGroup[1];
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_skipArrayForNegatives(int index, String id, CallbackInfo ci) {
        if (((Object) this) instanceof PolymerItemGroup) {
            GROUPS = GROUPS_OLD;
        }
    }
}
