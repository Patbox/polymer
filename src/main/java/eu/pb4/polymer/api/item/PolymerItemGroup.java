package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.InternalServerRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class PolymerItemGroup extends ItemGroup implements PolymerObject {
    public static List<ItemStack> items = new ArrayList<>();
    private final Text name;
    private final Identifier identifier;
    private ItemStack icon = ItemStack.EMPTY;

    private PolymerItemGroup(Identifier id, Text name) {
        super(0, id.toString());
        this.identifier = id;
        this.name = name;
    }

    public PolymerItemGroup setIcon(ItemStack stack) {
        this.icon = stack;
        return this;
    }

    @Override
    public ItemStack createIcon() {
        return this.icon;
    }

    @Override
    public Text getTranslationKey() {
        return this.name;
    }

    public Identifier getId() {
        return this.identifier;
    }

    public static PolymerItemGroup create(Identifier id, Text name, ItemStack icon) {
        return create(id, name).setIcon(icon);
    }

    public static PolymerItemGroup create(Identifier id, Text name) {
        var group = new PolymerItemGroup(id, name);

        InternalServerRegistry.ITEM_GROUPS.set(id, group);
        return group;
    }
}
