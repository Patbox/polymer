package eu.pb4.polymer.core.impl.other;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record PacketTooltipContext(PacketContext context, HolderLookup.Provider provider) implements Item.TooltipContext {
    @Override
    public HolderLookup.Provider registries() {
        return provider;
    }

    @Override
    public float tickRate() {
        if (PolymerCommonUtils.getPlayer(context) != null) {
            return PolymerCommonUtils.getPlayer(context).level().tickRateManager().tickrate();
        }

        return 20;
    }

    @Override
    public @Nullable MapItemSavedData mapData(MapId mapId) {
        try {
            if (PolymerCommonUtils.getPlayer(context) != null) {
                return PolymerCommonUtils.getPlayer(context).level().getMapData(mapId);
            }
        } catch (Throwable e) {
            // Failed to get data.
        }

        return null;
    }

    @Override
    public boolean isPeaceful() {
        if (PolymerCommonUtils.getPlayer(context) != null) {
            return PolymerCommonUtils.getPlayer(context).level().getDifficulty() == Difficulty.PEACEFUL;
        }


        return false;
    }
}

