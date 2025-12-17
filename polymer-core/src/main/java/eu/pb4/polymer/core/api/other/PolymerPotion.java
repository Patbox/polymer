package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.Potion;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PolymerPotion extends PolymerSyncedObject<Potion> {
    @Override
    @Nullable
    default Potion getPolymerReplacement(Potion potion, PacketContext context) {
        return null;
    }

    static void registerOverlay(Holder<Potion> entry, PolymerPotion overlay) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.POTION, entry.value(), overlay);
    }

    static void registerOverlay(Potion entry, PolymerPotion overlay) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.POTION, entry, overlay);
    }
}
