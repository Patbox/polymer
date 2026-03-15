package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.EntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;

public class MarkerElement extends GenericEntityElement {
    public MarkerElement() {
        this.dataTracker.set(ArmorStand.DATA_CLIENT_FLAGS, (byte) (ArmorStand.CLIENT_FLAG_MARKER | ArmorStand.CLIENT_FLAG_SMALL));
        this.dataTracker.set(EntityData.SILENT, true);
        this.dataTracker.set(EntityData.NO_GRAVITY, true);
        this.dataTracker.set(EntityData.FLAGS, (byte) ((1 << EntityData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.ARMOR_STAND;
    }
}
