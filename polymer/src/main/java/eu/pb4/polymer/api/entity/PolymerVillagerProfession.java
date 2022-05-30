package eu.pb4.polymer.api.entity;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.VillagerProfession;

public interface PolymerVillagerProfession extends PolymerObject {
    /**
     * Returns villager profession displayed to the client.
     * It should only return vanilla profession, unless you know client understands you custom one
     */
    VillagerProfession getPolymerProfession(VillagerProfession profession, ServerPlayerEntity player);
}
