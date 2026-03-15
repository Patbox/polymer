package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class MobAnchorElement extends GenericEntityElement {
    public MobAnchorElement() {
        this.dataTracker.set(SlimeEntityAccessor.getID_SIZE(), 0);
        this.dataTracker.set(EntityData.SILENT, true);
        this.dataTracker.set(EntityData.NO_GRAVITY, true);
        this.dataTracker.set(EntityData.FLAGS, (byte) ((1 << EntityData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.SLIME;
    }
}
