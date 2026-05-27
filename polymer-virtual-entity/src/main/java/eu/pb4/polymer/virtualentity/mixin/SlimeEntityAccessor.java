package eu.pb4.polymer.virtualentity.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractCubeMob.class)
public interface SlimeEntityAccessor {
    @Accessor
    static EntityDataAccessor<Integer> getID_SIZE() {
        throw new UnsupportedOperationException();
    }
}
