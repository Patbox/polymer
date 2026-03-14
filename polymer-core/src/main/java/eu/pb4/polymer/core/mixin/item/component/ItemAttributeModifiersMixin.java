package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.TransformingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.component.ItemAttributeModifiers;

@Mixin(ItemAttributeModifiers.class)
public class ItemAttributeModifiersMixin implements TransformingComponent {
    @Shadow @Final private List<ItemAttributeModifiers.Entry> modifiers;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }
        var list = new ArrayList<ItemAttributeModifiers.Entry>();
        for (var entry : this.modifiers) {
            if (!PolymerEntityUtils.isPolymerEntityAttribute(entry.attribute())) {
                list.add(entry);
            }
        }

        return new ItemAttributeModifiers(list);
    }

    @Override
    public boolean polymer$requireModification(PacketContext context) {
        for (var x : this.modifiers) {
            if (PolymerEntityUtils.isPolymerEntityAttribute(x.attribute())) {
                return true;
            }
        }
        return false;
    }
}
