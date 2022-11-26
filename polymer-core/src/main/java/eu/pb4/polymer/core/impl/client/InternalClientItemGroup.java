package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.ItemGroupExtra;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class InternalClientItemGroup extends ItemGroup implements PolymerObject, ItemGroupExtra {
    private final Identifier identifier;

    public InternalClientItemGroup(Row row, int column, Identifier identifier, Text name, ItemStack stack) {
        super(row, column, Type.CATEGORY, name, () -> stack.copy(), (a, b, c) -> {});
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Identifier getId() {
        return PolymerImplUtils.id( "group/" + this.identifier.getNamespace() + "/" + this.identifier.getPath());
    }

    @Override
    public PolymerItemGroupUtils.Contents polymer$getContentsWith(FeatureSet enabledFeatures, boolean operatorEnabled) {
        return null;
    }

    @Override
    public boolean polymer$isSyncable() {
        return false;
    }
}
