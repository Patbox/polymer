package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.interfaces.IngredientExtension;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Optional;

@Mixin(value = Ingredient.class, priority = 1200)
public class IngredientMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/PacketCodec;", ordinal = 0))
    private static PacketCodec<RegistryByteBuf, Ingredient> modifyRegularCodec(PacketCodec<RegistryByteBuf, Ingredient> original) {
        if (!PolymerImpl.EXTENDED_RECIPE_INGREDIENTS) {
            return original;
        }

        return new IngredientExtension.BasePacketCodec(original);
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/PacketCodec;", ordinal = 1))
    private static PacketCodec<RegistryByteBuf, Optional<Ingredient>> modifyOptionalCodec(PacketCodec<RegistryByteBuf, Optional<Ingredient>> original) {
        if (!PolymerImpl.EXTENDED_RECIPE_INGREDIENTS) {
            return original;
        }
        return new IngredientExtension.OptionalPacketCodec(original);
    }
}
