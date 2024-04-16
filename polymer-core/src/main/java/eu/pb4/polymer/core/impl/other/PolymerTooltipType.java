package eu.pb4.polymer.core.impl.other;


import net.minecraft.client.item.TooltipType;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.Item;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

public record PolymerTooltipType(boolean advanced, boolean creative) implements TooltipType {
    public static final PolymerTooltipType BASIC = new PolymerTooltipType(false, false);
    public static final PolymerTooltipType ADVANCED = new PolymerTooltipType(true, false);

    public PolymerTooltipType withCreative() {
        return new PolymerTooltipType(this.advanced, true);
    }

    public static PolymerTooltipType of(TooltipType context) {
        return new PolymerTooltipType(context.isAdvanced(), context.isCreative());
    }

    @Override
    public boolean isAdvanced() {
        return this.advanced;
    }

    @Override
    public boolean isCreative() {
        return this.creative;
    }
}
