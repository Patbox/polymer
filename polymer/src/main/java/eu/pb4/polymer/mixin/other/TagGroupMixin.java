package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.tag.TagGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TagGroup.class)
public interface TagGroupMixin {
    @ModifyVariable(method = "method_33156", at = @At(value = "STORE"), ordinal = 0)
    private static List<Object> polymer_skipEntries(List<Object> list) {
        var newList = new ArrayList<>();
        for (var entry : list) {
            if (!PolymerObject.PREDICATE.test(entry)) {
                newList.add(entry);
            }
        }
        return newList;
    }
}