package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolymerStatusEffect extends PolymerSyncedObject<StatusEffect> {
    @Nullable
    default ItemStack getPolymerIcon(ServerPlayerEntity player) {
        var icon = Items.POTION.getDefaultStack();
        icon.getOrCreateNbt().putInt("CustomPotionColor", ((StatusEffect) this).getColor());
        return icon;
    }

    @Override
    @Nullable
    default StatusEffect getPolymerReplacement(ServerPlayerEntity player) {
        return null;
    }
}
