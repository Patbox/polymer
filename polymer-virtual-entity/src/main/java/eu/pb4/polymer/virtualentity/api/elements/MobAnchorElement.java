package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.EntityData;
import eu.pb4.polymer.virtualentity.mixin.SlimeEntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class MobAnchorElement extends GenericEntityElement {
    public MobAnchorElement() {
        this.syncedData.set(SlimeEntityAccessor.getID_SIZE(), 0);
        this.syncedData.set(EntityData.SILENT, true);
        this.syncedData.set(EntityData.NO_GRAVITY, true);
        this.syncedData.set(EntityData.FLAGS, (byte) ((1 << EntityData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityTypes.SLIME;
    }
}
