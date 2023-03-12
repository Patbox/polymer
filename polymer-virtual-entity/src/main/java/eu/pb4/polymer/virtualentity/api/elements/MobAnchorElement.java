package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class MobAnchorElement extends GenericEntityElement {
    public MobAnchorElement() {
        this.dataTracker.set(SlimeEntityAccessor.getSLIME_SIZE(), 0);
        this.dataTracker.set(EntityTrackedData.SILENT, true);
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.SLIME;
    }
}
