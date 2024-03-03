package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface PolymerStatusEffect extends PolymerSyncedObject<StatusEffect> {
    @Nullable
    default ItemStack getPolymerIcon(ServerPlayerEntity player) {
        var icon = Items.POTION.getDefaultStack();
        icon.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),
                Optional.of(((StatusEffect) this).getColor()), List.of() ));
        return icon;
    }

    @Override
    @Nullable
    default StatusEffect getPolymerReplacement(ServerPlayerEntity player) {
        return null;
    }
}
