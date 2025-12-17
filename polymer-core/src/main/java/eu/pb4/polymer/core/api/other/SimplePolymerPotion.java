package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;

public class SimplePolymerPotion extends Potion implements PolymerPotion {
    public SimplePolymerPotion(MobEffectInstance... effects) {
        super((String)null, effects);
    }

    public SimplePolymerPotion(@Nullable String baseName, MobEffectInstance... effects) {
        super(baseName, effects);
    }
}
