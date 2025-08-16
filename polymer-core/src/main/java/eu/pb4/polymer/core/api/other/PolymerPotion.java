package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PolymerPotion extends PolymerSyncedObject<Potion> {
    @Override
    @Nullable
    default Potion getPolymerReplacement(Potion potion, PacketContext context) {
        return null;
    }

    static void registerOverlay(RegistryEntry<Potion> entry, PolymerPotion overlay) {
        PolymerSyncedObject.setSyncedObject(Registries.POTION, entry.value(), overlay);
    }

    static void registerOverlay(Potion entry, PolymerPotion overlay) {
        PolymerSyncedObject.setSyncedObject(Registries.POTION, entry, overlay);
    }
}
