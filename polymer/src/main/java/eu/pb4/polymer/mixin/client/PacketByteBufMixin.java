package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(PacketByteBuf.class)
public class PacketByteBufMixin {
    @Redirect(method = "readRegistryValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IndexedIterable;get(I)Ljava/lang/Object;"))
    private Object polymer_justInCaseRemap(IndexedIterable instance, int i) {
        if (instance == Block.STATE_IDS) {
            return InternalClientRegistry.decodeState(i);
        } else if (instance == Registries.ENTITY_TYPE) {
            return InternalClientRegistry.decodeEntity(i);
        } else if (instance == Registries.ENCHANTMENT) {
            return InternalClientRegistry.decodeEnchantment(i);
        } else if (instance == Registries.ITEM) {
            return InternalClientRegistry.decodeItem(i);
        } else if (instance == Registries.VILLAGER_PROFESSION) {
            return InternalClientRegistry.decodeVillagerProfession(i);
        } else if (instance == Registries.STATUS_EFFECT) {
            return InternalClientRegistry.decodeStatusEffect(i);
        }

        return instance.get(i);
    }
}
