package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerRegistry;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.PolymerImplUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    public static final SimpleEvent<ItemGroupEventListener> LIST_EVENT = new SimpleEvent<>();
    private final Text name;
    private final Identifier identifier;
    private boolean sync;
    private ItemStack icon = PolymerImplUtils.getNoTextureItem().copy();
    private Consumer<DefaultedList<ItemStack>> customAppend = null;
    private Supplier<ItemStack> cachedIcon;

    private PolymerItemGroup(Identifier id, Text name, boolean sync) {
        super(0, id.toString().replace(":", "_").toLowerCase(Locale.ROOT));
        this.identifier = id;
        this.name = name;
        this.sync = sync;
    }

    /**
     * Creates new ItemGroup
     */
    public static PolymerItemGroup create(Identifier id, Text name, ItemStack icon) {
        return create(id, name).setIcon(() -> icon);
    }

    public static PolymerItemGroup create(Identifier id, Text name, Supplier<ItemStack> icon) {
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
    public PolymerItemGroup setIcon(Supplier<ItemStack> stack) {
        this.cachedIcon = stack;
        this.icon = null;
        return this;
    }

    /**
     * Sets icon of ItemGroup
     */
    public PolymerItemGroup setIcon(ItemStack stack) {
        this.cachedIcon = null;
        if (stack.isEmpty()) {
            this.icon = PolymerImplUtils.getNoTextureItem().copy();
        } else {
            this.icon = stack.copy();
        }
        return this;
    }

    public PolymerItemGroup setSync(boolean value) {
        this.sync = value;
        return this;
    }

    public PolymerItemGroup setCustomAppend(Consumer<DefaultedList<ItemStack>> itemListConsumer) {
        this.customAppend = itemListConsumer;
        return this;
    }

    @Override
    public ItemStack createIcon() {
        if (this.cachedIcon != null) {
            this.icon = this.cachedIcon.get();
            this.cachedIcon = null;
        }

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
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        if (this.customAppend == null) {
            super.appendStacks(stacks);
        } else {
            this.customAppend.accept(stacks);
        }
    }

    @Override
    public boolean shouldSyncWithPolymerClient(ServerPlayerEntity player) {
        return this.sync;
    }

    @FunctionalInterface
    public interface ItemGroupEventListener {
        void onItemGroupSync(ServerPlayerEntity player, ItemGroupListBuilder builder);
    }

    public interface ItemGroupListBuilder {
        void add(PolymerItemGroup group);

        void remove(PolymerItemGroup group);
    }
}
