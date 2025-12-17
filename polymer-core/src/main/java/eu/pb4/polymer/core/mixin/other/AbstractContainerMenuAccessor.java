package eu.pb4.polymer.core.mixin.other;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerMenu.class)
public interface AbstractContainerMenuAccessor {
    @Accessor
    ContainerSynchronizer getSynchronizer();
}
