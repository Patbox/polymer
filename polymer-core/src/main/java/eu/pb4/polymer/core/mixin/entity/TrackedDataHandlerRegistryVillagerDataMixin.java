package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/entity/data/TrackedDataHandlerRegistry$6")
public class TrackedDataHandlerRegistryVillagerDataMixin {
    @Redirect(method = "write(Lnet/minecraft/network/PacketByteBuf;Lnet/minecraft/village/VillagerData;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/village/VillagerData;getProfession()Lnet/minecraft/village/VillagerProfession;"))
    private VillagerProfession polymer_replaceWithPolymer(VillagerData instance) {
        var profession = instance.getProfession();
        var polymerProf = PolymerEntityUtils.getPolymerProfession(profession);
        return polymerProf != null ? polymerProf.getPolymerProfession(profession, PolymerUtils.getPlayerContext()) : profession;
    }
}
