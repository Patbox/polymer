package eu.pb4.polymer.core.impl.other;


import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public record PolymerTooltipType(boolean advanced, boolean creative) implements TooltipFlag {
    public static final PolymerTooltipType BASIC = new PolymerTooltipType(false, false);
    public static final PolymerTooltipType ADVANCED = new PolymerTooltipType(true, false);

    public PolymerTooltipType withCreative() {
        return new PolymerTooltipType(this.advanced, true);
    }

    public static PolymerTooltipType of(TooltipFlag context) {
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
