package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.CreativeModeTabExtra;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class InternalClientItemGroup extends CreativeModeTab implements PolymerObject, CreativeModeTabExtra {
    private final Identifier identifier;

    public InternalClientItemGroup(Row row, int column, Identifier identifier, Component name, ItemStack stack) {
        super(row, column, Type.CATEGORY, name, stack::copy, (a, c) -> {});
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Identifier getId() {
        return PolymerImplUtils.id( "group/" + this.identifier.getNamespace() + "/" + this.identifier.getPath());
    }


    @Override
    public PolymerItemGroupUtils.Contents polymer$getContentsWith(Identifier id, FeatureFlagSet enabledFeatures, boolean operatorEnabled, HolderLookup.Provider lookup) {
        return null;
    }

    @Override
    public boolean polymer$isSyncable() {
        return false;
    }

    public ResourceKey<CreativeModeTab> getKey() {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, this.identifier);
    }
}
