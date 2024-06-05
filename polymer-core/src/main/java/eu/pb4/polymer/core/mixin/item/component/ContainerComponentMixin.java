package eu.pb4.polymer.core.mixin.item.component;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.TransformingDataComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ContainerComponent.class)
public abstract class ContainerComponentMixin implements TransformingDataComponent {

    @Shadow public abstract Iterable<ItemStack> iterateNonEmpty();

    @Shadow public abstract Stream<ItemStack> stream();

    @Override
    public Object polymer$getTransformed(ServerPlayerEntity player) {
        if (!polymer$requireModification(player)) {
            return this;
        }

        List<ItemStack> transformedItems = new ArrayList<>();
        stream()
                .map(stack -> PolymerItemUtils.getPolymerItemStack(stack, player.getRegistryManager(), player))
                .forEachOrdered(transformedItems::add);
        return ContainerComponent.fromStacks(transformedItems);
    }

    @Override
    public boolean polymer$requireModification(ServerPlayerEntity player) {
        for (ItemStack stack : iterateNonEmpty()) {
            if (PolymerItemUtils.isPolymerServerItem(stack, player)) {
                return true;
            }
        }
        return false;
    }
}
