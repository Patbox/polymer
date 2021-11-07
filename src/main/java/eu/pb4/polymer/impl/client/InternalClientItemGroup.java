package eu.pb4.polymer.impl.client;

import eu.pb4.polymer.api.utils.PolymerObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class InternalClientItemGroup extends ItemGroup implements PolymerObject {
    private final ItemStack icon;
    private final Text name;
    private final List<ItemStack> stacks;
    private final Identifier identifier;

    public InternalClientItemGroup(int index, Identifier identifier, String id, Text name, ItemStack stack, List<ItemStack> stackList) {
        super(index, id);
        this.identifier = identifier;
        this.name = name;
        this.icon = stack;
        this.stacks = stackList;
    }

    @Override
    public ItemStack createIcon() {
        return icon;
    }

    @Override
    public Text getDisplayName() {
        return this.name;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        stacks.addAll(this.stacks);
    }

    public Collection<ItemStack> getStacks() {
        return this.stacks;
    }
}
