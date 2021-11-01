package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.interfaces.MutableSearchableContainer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.search.IdentifierSearchableContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(IdentifierSearchableContainer.class)
public class IdentifierSearchableContainerMixin implements MutableSearchableContainer {
    @Shadow @Final private List<?> entries;

    @Shadow @Final private Object2IntMap<?> entryIds;

    @Mutable
    @Shadow @Final private Function<Object, Stream<Identifier>> identifierFinder;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void polymer_replaceIdentifierGetter(Function<Object, Stream<Identifier>> identifierFinder, CallbackInfo ci) {
        try {
            if (identifierFinder.apply(Items.POTATO.getDefaultStack()).collect(Collectors.toList()).contains(new Identifier("potato"))) {
                this.identifierFinder = (obj) -> {
                    if (obj instanceof ItemStack stack) {
                        var id = PolymerItemUtils.getPolymerIdentifier(stack);

                        if (id != null) {
                            return Stream.of(id);
                        }
                    }

                    return identifierFinder.apply(obj);
                };
            }
        } catch (Exception e) {
            // Silence!
        }
    }

    @Override
    public void polymer_remove(Object obj) {
        this.entries.remove(obj);
        this.entryIds.removeInt(obj);
    }

    @Override
    public void polymer_removeIf(Predicate<Object> predicate) {
        for (var entry : new ArrayList<>(this.entries)) {
            if (predicate.test(entry)) {
                this.entries.remove(entry);
                this.entryIds.removeInt(entry);
            }
        }
    }
}
