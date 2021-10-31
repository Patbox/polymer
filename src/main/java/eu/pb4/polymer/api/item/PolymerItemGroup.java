package eu.pb4.polymer.api.item;

import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.networking.ServerPacketBuilders;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class PolymerItemGroup extends ItemGroup implements PolymerObject {
    public static final SimpleEvent<ItemGroupSyncEventListener> SYNC_EVENT = new SimpleEvent<>();
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

    public static PolymerItemGroup create(Identifier id, Text name, ItemStack icon) {
        return create(id, name).setIcon(icon);
    }

    public static PolymerItemGroup create(Identifier id, Text name) {
        var group = new PolymerItemGroup(id, name, true);

        InternalServerRegistry.ITEM_GROUPS.set(id, group);
        return group;
    }

    public static PolymerItemGroup createPrivate(Identifier id, Text name) {
        var group = new PolymerItemGroup(id, name, false);

        InternalServerRegistry.ITEM_GROUPS.set(id, group);
        return group;
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

    @Override
    public boolean syncWithPolymerClients(ServerPlayerEntity player) {
        return this.sync;
    }

    @FunctionalInterface
    public interface ItemGroupSyncEventListener {
        void onItemGroupSync(ServerPlayerEntity player, ItemGroupSyncer helper);
    }

    public interface ItemGroupSyncer {
        void send(PolymerItemGroup group);

        void remove(PolymerItemGroup group);
    }
}
