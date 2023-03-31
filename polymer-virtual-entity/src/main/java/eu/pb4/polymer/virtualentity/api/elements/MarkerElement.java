package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;

public class MarkerElement extends GenericEntityElement {
    public MarkerElement() {
        this.dataTracker.set(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte) (ArmorStandEntity.MARKER_FLAG | ArmorStandEntity.SMALL_FLAG));
        this.dataTracker.set(EntityTrackedData.SILENT, true);
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.ARMOR_STAND;
    }
}
