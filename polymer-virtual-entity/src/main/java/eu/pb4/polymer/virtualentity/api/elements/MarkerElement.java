package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;

public class MarkerElement extends GenericEntityElement {
    public MarkerElement() {
        this.dataTracker.set(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_MARKER | ArmorStand.CLIENT_FLAG_SMALL));
        this.dataTracker.set(EntityTrackedData.SILENT, true);
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.ARMOR_STAND;
    }
}
