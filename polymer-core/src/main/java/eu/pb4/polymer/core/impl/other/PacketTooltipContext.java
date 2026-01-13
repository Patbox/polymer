package eu.pb4.polymer.core.impl.other;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public record PacketTooltipContext(PacketContext context) implements Item.TooltipContext {
    @Override
    public HolderLookup.Provider registries() {
        return context.getRegistryWrapperLookup();
    }

    @Override
    public float tickRate() {
        if (context.getPlayer() != null) {
            return context.getPlayer().level().tickRateManager().tickrate();
        }

        return 20;
    }

    @Override
    public @Nullable MapItemSavedData mapData(MapId mapId) {
        try {
            if (context.getPlayer() != null) {
                return context.getPlayer().level().getMapData(mapId);
            }
        } catch (Throwable e) {
            // Failed to get data.
        }

        return null;
    }

    @Override
    public boolean isPeaceful() {
        if (context.getPlayer() != null) {
            return context.getPlayer().level().getDifficulty() == Difficulty.PEACEFUL;
        }


        return false;
    }
}

