package eu.pb4.polymer.impl.client.compat;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

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
