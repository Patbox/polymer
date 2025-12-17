package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.virtualentity.mixin.accessors.InteractionAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;

public class InteractionTrackedData {
    public static final EntityDataAccessor<Float> WIDTH = InteractionAccessor.getDATA_WIDTH_ID();
    public static final EntityDataAccessor<Float> HEIGHT = InteractionAccessor.getDATA_HEIGHT_ID();
    public static final EntityDataAccessor<Boolean> RESPONSE = InteractionAccessor.getDATA_RESPONSE_ID();
}
