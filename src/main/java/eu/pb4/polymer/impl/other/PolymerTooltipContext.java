package eu.pb4.polymer.impl.other;

import net.minecraft.client.item.TooltipContext;

public enum PolymerTooltipContext implements TooltipContext {
    NORMAL(false),
    ADVANCED(true);

    private final boolean advanced;

    PolymerTooltipContext(boolean advanced) {
        this.advanced = advanced;
    }

    @Override
    public boolean isAdvanced() {
        return this.advanced;
    }

    public TooltipContext toVanilla() {
        return this.isAdvanced() ? Default.ADVANCED : Default.NORMAL;
    }

    public static PolymerTooltipContext of(TooltipContext context) {
        return context.isAdvanced() ? ADVANCED : NORMAL;
    }
}
