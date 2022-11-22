package eu.pb4.polymer.api.other;

import eu.pb4.polymer.api.utils.PolymerSyncedObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolymerEnchantment extends PolymerSyncedObject<Enchantment> {
    @Nullable
    @Override
    default Enchantment getPolymerReplacement(ServerPlayerEntity player) {
        return null;
    }
}
