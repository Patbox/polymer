package eu.pb4.polymer.virtualentity.api.tracker;

import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SimpleDataTracker implements DataTrackerLike {
    private final Int2ObjectMap<Entry<?>> entries = new Int2ObjectOpenHashMap();
    private boolean dirty;

    @SuppressWarnings("rawtypes")
    public SimpleDataTracker(EntityType<?> baseEntity) {
        var entries = InternalEntityHelpers.getExampleTrackedDataOfEntityType(baseEntity);

        for (var x : entries.int2ObjectEntrySet()) {
            this.entries.put(x.getIntKey(), new Entry(x.getValue().getData(), x.getValue().get()));
        }
    }

    @Override
    public <T> T get(TrackedData<T> data) {
        var entry = this.getEntry(data);

        return entry != null ? entry.get() : null;
    }

    @Nullable
    private <T> Entry<T> getEntry(TrackedData<T> data) {
        var x = this.entries.get(data.getId());

        if (x.data != data) {
            return null;
        }

        return (Entry<T>) x;
    }

    @Override
    public <T> void set(TrackedData<T> key, T value) {
        var entry = getEntry(key);
        if (entry != null && ObjectUtils.notEqual(value, entry.get())) {
            entry.set(value);
            entry.setDirty(true);
            this.dirty = true;
        }
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    @Nullable
    public List<DataTracker.SerializedEntry<?>> getDirtyEntries() {
        List<DataTracker.SerializedEntry<?>> list = null;
        if (this.dirty) {
            ObjectIterator var2 = this.entries.values().iterator();

            while(var2.hasNext()) {
                SimpleDataTracker.Entry<?> entry = (SimpleDataTracker.Entry)var2.next();
                if (entry.isDirty()) {
                    entry.setDirty(false);
                    if (list == null) {
                        list = new ArrayList();
                    }

                    list.add(entry.toSerialized());
                }
            }
        }

        this.dirty = false;
        return list;
    }

    @Override
    @Nullable
    public List<DataTracker.SerializedEntry<?>> getChangedEntries() {
        List<DataTracker.SerializedEntry<?>> list = null;
        ObjectIterator var2 = this.entries.values().iterator();

        while(var2.hasNext()) {
            SimpleDataTracker.Entry<?> entry = (SimpleDataTracker.Entry)var2.next();
            if (!entry.isUnchanged()) {
                if (list == null) {
                    list = new ArrayList();
                }

                list.add(entry.toSerialized());
            }
        }

        return list;
    }

    @Override
    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public static class Entry<T> {
        final TrackedData<T> data;
        T value;
        private final T initialValue;
        private boolean dirty;

        public Entry(TrackedData<T> data, T value) {
            this.data = data;
            this.initialValue = value;
            this.value = value;
        }

        public TrackedData<T> getData() {
            return this.data;
        }

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean dirty) {
            this.dirty = dirty;
        }

        public boolean isUnchanged() {
            return this.initialValue.equals(this.value);
        }

        public DataTracker.SerializedEntry<T> toSerialized() {
            return DataTracker.SerializedEntry.of(this.data, this.value);
        }
    }
}