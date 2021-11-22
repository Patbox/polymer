package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.InternalServerRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * An server side item group that can be synchronized with polymer clients
 * It also has it's own server side functionality
 */
public final class PolymerItemGroup extends ItemGroup implements PolymerObject {
    /**
     * Registry of all PolymerItemGroups
     */
    public static final PolymerRegistry<PolymerItemGroup> REGISTRY = InternalServerRegistry.ITEM_GROUPS;

    /**
     * Even called on synchronization of PolymerItemGroups
     */
    public static final SimpleEvent<ItemGroupListEventListener> LIST_EVENT = new SimpleEvent<>();
    public static List<ItemStack> items = new ArrayList<>();
    private final Text name;
    private final Identifier identifier;
    private final boolean sync;
    private ItemStack icon = ItemStack.EMPTY;
    private PolymerItemGroup(Identifier id, Text name, boolean sync) {
        super(0, id.toString());
        this.identifier = id;
        this.name = name;
        this.sync = sync;
    }

    /**
     * Creates new ItemGroup
     */
    public static PolymerItemGroup create(Identifier id, Text name, ItemStack icon) {
        return create(id, name).setIcon(icon);
    }

    /**
     * Creates new ItemGroup
     */
    public static PolymerItemGroup create(Identifier id, Text name) {
        var group = new PolymerItemGroup(id, name, true);

        InternalServerRegistry.ITEM_GROUPS.set(id, group);
        return group;
    }

    /**
     * Creates new ItemGroup, which isn't synced by default
     */
    public static PolymerItemGroup createPrivate(Identifier id, Text name) {
        var group = new PolymerItemGroup(id, name, false);

        InternalServerRegistry.ITEM_GROUPS.set(id, group);
        return group;
    }

    /**
     * Sets icon of ItemGroup
     */
    public PolymerItemGroup setIcon(ItemStack stack) {
        this.icon = stack;
        return this;
    }

    @Override
    public ItemStack createIcon() {
        return this.icon;
    }

    @Override
    public Text getDisplayName() {
        return this.name;
    }

    public Identifier getId() {
        return this.identifier;
    }

    @Override
    public boolean shouldSyncWithPolymerClient(ServerPlayerEntity player) {
        return this.sync;
    }

    @FunctionalInterface
    public interface ItemGroupListEventListener {
        void onItemGroupSync(ServerPlayerEntity player, ItemGroupSyncer helper);
    }

    public interface ItemGroupSyncer {
        void send(PolymerItemGroup group);

        void remove(PolymerItemGroup group);
    }
}
