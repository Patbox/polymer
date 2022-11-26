package eu.pb4.polymer.core.mixin.client.item;

import eu.pb4.polymer.core.impl.client.interfaces.MutableSearchableContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.search.IdentifierSearchProvider;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
@Mixin(IdentifierSearchProvider.class)
public class IdentifierSearchableContainerMixin implements MutableSearchableContainer {
    /*@Shadow @Final private List<?> entries;

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
    }*/

    @Override
    public void polymer_remove(Object obj) {
        //this.entries.remove(obj);
        //this.entryIds.removeInt(obj);
    }

    @Override
    public void polymer_removeIf(Predicate<Object> predicate) {
        /*for (var entry : new ArrayList<>(this.entries)) {
            if (predicate.test(entry)) {
                this.entries.remove(entry);
                this.entryIds.removeInt(entry);
            }
        }*/
    }
}
