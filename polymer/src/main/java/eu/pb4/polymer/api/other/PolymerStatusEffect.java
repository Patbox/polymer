package eu.pb4.polymer.api.other;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolymerStatusEffect extends PolymerObject {

    /**
     * Returns the effect to be displayed on the client's HUD.
     * Note that particle color is determined by the server, so this will not affect them.
     *
     * @return Vanilla status effect, or <code>null</>null if nothing is to be displayed by the client
     */
    default StatusEffect getPolymerStatusEffect() {
        return null;
    }

    default StatusEffect getPolymerStatusEffect(ServerPlayerEntity player) {
        return getPolymerStatusEffect();
    }

    @Nullable
    default ItemStack getPolymerIcon(ServerPlayerEntity player) {
        var icon = Items.POTION.getDefaultStack();
        icon.getOrCreateNbt().putInt("CustomPotionColor", ((StatusEffect) this).getColor());
        return icon;
    }
}
