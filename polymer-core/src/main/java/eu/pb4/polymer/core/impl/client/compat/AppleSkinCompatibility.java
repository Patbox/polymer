package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import org.jetbrains.annotations.ApiStatus;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

@ApiStatus.Internal
public class AppleSkinCompatibility implements AppleSkinApi {
    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(event -> {
            var id = PolymerItemUtils.getPolymerIdentifier(event.itemStack);
            if (id != null) {
                var item = InternalClientRegistry.ITEMS.get(id);

                if (item != null) {
                    event.modifiedFoodValues = new FoodValues(item.foodValue(), item.saturation());
                }
            }
        });
    }
}
