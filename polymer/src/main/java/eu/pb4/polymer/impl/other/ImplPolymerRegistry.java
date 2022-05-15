package eu.pb4.polymer.impl.other;

import eu.pb4.polymer.api.utils.PolymerRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@ApiStatus.Internal
public class ImplPolymerRegistry<T> implements PolymerRegistry<T> {
    private final Map<Identifier, T> entryMap = new HashMap<>();
    private final Int2ObjectMap<T> rawIdMap = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<T> entryIdMap = new Object2IntOpenHashMap<>();
    private final Map<T, Identifier> identifierMap = new HashMap<>();
    private final T defaultValue;
    private final Identifier defaultIdentifier;
    private final String name;
    private final String shortName;

    private int currentId = 0;

    public ImplPolymerRegistry(String name, String shortName) {
        this(name, shortName, null, null);
    }
    public ImplPolymerRegistry(String name, String shortName, @Nullable Identifier defaultIdentifier, @Nullable T defaultValue) {
        this.name = name;
        this.shortName = shortName;
        this.defaultValue = defaultValue;
        this.defaultIdentifier = defaultIdentifier;

        this.rawIdMap.defaultReturnValue(this.defaultValue);
        this.entryIdMap.defaultReturnValue(-1);
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public T get(Identifier identifier) {
        return this.entryMap.getOrDefault(identifier, this.defaultValue);
    }

    @Override
    public T get(int id) {
        return this.rawIdMap.get(id);
    }

    @Override
    public Identifier getId(T entry) {
        return this.identifierMap.getOrDefault(entry, this.defaultIdentifier);
    }

    @Override
    public int getRawId(T entry) {
        return this.entryIdMap.getInt(entry);
    }

    @Override
    public int size() {
        return this.entryMap.size();
    }

    public void set(Identifier identifier, int rawId, T entry) {
        this.entryMap.put(identifier, entry);
        this.identifierMap.put(entry, identifier);
        this.rawIdMap.put(rawId, entry);
        this.entryIdMap.put(entry, rawId);
        this.currentId = Math.max(this.currentId, rawId) + 1;
    }

    public void set(Identifier identifier, T entry) {
        this.set(identifier, this.currentId, entry);
    }

    public void clear() {
        this.rawIdMap.clear();
        this.identifierMap.clear();
        this.entryMap.clear();
        this.entryIdMap.clear();
        this.currentId = 0;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.rawIdMap.values().iterator();
    }

    public Iterable<Identifier> ids() {
        return () -> ImplPolymerRegistry.this.identifierMap.values().iterator();
    }

    @Override
    public Iterable<Map.Entry<Identifier, T>> entries() {
        return () -> this.entryMap.entrySet().iterator();
    }

    public void remove(T entry) {
        if (this.identifierMap.containsKey(entry)) {
            var id = this.identifierMap.get(entry);
            var rawId = this.entryIdMap.getInt(entry);
            this.rawIdMap.remove(rawId);
            this.entryMap.remove(id);
            this.entryIdMap.removeInt(entry);
            this.identifierMap.remove(entry);
        }
    }
}
