package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public interface PolymerMobEffect extends PolymerSyncedObject<MobEffect> {
    static void registerOverlay(MobEffect effect) {
        registerOverlay(effect, (e, c) -> null);
    }

    static void registerOverlay(MobEffect effect, PolymerSyncedObject<MobEffect> overlay) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.MOB_EFFECT, effect, overlay);
    }

    @Nullable
    default ItemStack getPolymerIcon(MobEffect effect, ServerPlayer player) {
        var icon = Items.POTION.getDefaultInstance();
        icon.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(),
                Optional.of(((MobEffect) this).getColor()), List.of(), Optional.empty()));
        return icon;
    }

    @Override
    @Nullable
    default MobEffect getPolymerReplacement(MobEffect potion, PacketContext context) {
        return null;
    }
}
