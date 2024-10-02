package eu.pb4.polymer.core.mixin.item.packet;

import eu.pb4.polymer.core.impl.interfaces.SkipCheck;
import net.minecraft.recipe.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SlotDisplay.TagSlotDisplay.class)
public class TagSlotDisplayMixin implements SkipCheck {
    @Unique
    private boolean skipped = false;

    @Override
    public boolean polymer$skipped() {
        return this.skipped;
    }

    @Override
    public void polymer$setSkipped() {
        this.skipped = true;
    }
}
