package eu.pb4.polymer.api.other;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolymerEnchantment extends PolymerObject {
    @Nullable
    default Enchantment getPolymerEnchantment(@Nullable ServerPlayerEntity player) {
        return null;
    }
}
