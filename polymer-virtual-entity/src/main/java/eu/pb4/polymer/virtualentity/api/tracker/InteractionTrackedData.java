package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.virtualentity.mixin.accessors.InteractionEntityAccessor;
import net.minecraft.entity.data.TrackedData;

public class InteractionTrackedData {
    public static final TrackedData<Float> WIDTH = InteractionEntityAccessor.getWIDTH();
    public static final TrackedData<Float> HEIGHT = InteractionEntityAccessor.getHEIGHT();
    public static final TrackedData<Boolean> RESPONSE = InteractionEntityAccessor.getRESPONSE();
}
