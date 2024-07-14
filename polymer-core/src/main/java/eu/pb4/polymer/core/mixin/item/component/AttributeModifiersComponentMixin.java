package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.TransformingComponent;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

@Mixin(AttributeModifiersComponent.class)
public class AttributeModifiersComponentMixin implements TransformingComponent {
    @Shadow @Final private List<AttributeModifiersComponent.Entry> modifiers;

    @Shadow @Final private boolean showInTooltip;

    @Override
    public Object polymer$getTransformed(PacketContext context) {
        if (!polymer$requireModification(context)) {
            return this;
        }
        var list = new ArrayList<AttributeModifiersComponent.Entry>();
        for (var entry : this.modifiers) {
            if (!PolymerEntityUtils.isPolymerEntityAttribute(entry.attribute())) {
                list.add(entry);
            }
        }

        return new AttributeModifiersComponent(list, this.showInTooltip);
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
