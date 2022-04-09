package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.entity.PolymerVillagerProfession;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.registry.DefaultedRegistry;
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
        return profession instanceof PolymerVillagerProfession polymer ? polymer.getPolymerProfession(PolymerUtils.getPlayer()) : profession;
    }


    @Environment(EnvType.CLIENT)
    @Redirect(method = "read(Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/village/VillagerData;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceWithPolymer(DefaultedRegistry instance, int index) {
        return InternalClientRegistry.decodeVillagerProfession(index);
    }
}
