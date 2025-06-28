package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

public interface PolymerStatusEffect extends PolymerSyncedObject<StatusEffect> {
    static void registerOverlay(StatusEffect effect) {
        registerOverlay(effect, (e, c) -> null);
    }

    static void registerOverlay(StatusEffect effect, PolymerSyncedObject<StatusEffect> overlay) {
        PolymerSyncedObject.setSyncedObject(Registries.STATUS_EFFECT, effect, overlay);
    }

    @Nullable
    default ItemStack getPolymerIcon(StatusEffect effect, ServerPlayerEntity player) {
        var icon = Items.POTION.getDefaultStack();
        icon.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(),
                Optional.of(((StatusEffect) this).getColor()), List.of(), Optional.empty()));
        return icon;
    }

    @Override
    @Nullable
    default StatusEffect getPolymerReplacement(StatusEffect potion, PacketContext context) {
        return null;
    }
}
