package eu.pb4.polymer.virtualentity.api.data;

import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;

public class SimpleSynchedEntityData implements SynchedEntityDataLike {
    private final Entry<?>[] entries;
    private boolean dirty;

    @SuppressWarnings("rawtypes")
    public SimpleSynchedEntityData(EntityType<?> baseEntity) {
        var entries = InternalEntityHelpers.getExampleTrackedDataOfEntityType(baseEntity);
        this.entries = new Entry[entries.length];

        for (int i = 0; i < entries.length; i++) {
            var x = entries[i];
            //noinspection unchecked
            this.entries[i] = new Entry(x.getAccessor(), x.getValue());
        }
    }

    @Override
    public <T> T get(EntityDataAccessor<T> data) {
        var entry = this.getEntry(data);
        return entry != null ? entry.get() : null;
    }

    @Nullable
    public <T> Entry<T> getEntry(EntityDataAccessor<T> data) {
        if (data.id() > this.entries.length) {
            return null;
        }

        var x = this.entries[data.id()];

        if (x.data != data) {
            return null;
        }

        //noinspection unchecked
        return (Entry<T>) x;
    }

    @Override
    public boolean isDirty(EntityDataAccessor<?> key) {
        var x = getEntry(key);
        return x != null && x.isDirty();
    }

    @Override
    public <T> void set(EntityDataAccessor<T> key, T value, boolean forceDirty) {
        var entry = getEntry(key);
        if (entry != null && (forceDirty || ObjectUtils.notEqual(value, entry.get()))) {
            entry.set(value);
            entry.setDirty(true);
            this.dirty = true;
        }
    }

    @Override
    public <T> void setDirty(EntityDataAccessor<T> key, boolean isDirty) {
        var entry = getEntry(key);
        if (entry != null) {
            entry.setDirty(isDirty);
            this.dirty |= isDirty;
        }
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    @Nullable
    public List<SynchedEntityData.DataValue<?>> getDirtyEntries() {
        List<SynchedEntityData.DataValue<?>> list = null;
        if (this.dirty) {
            for (int i = 0; i < this.entries.length; i++) {
                var entry = this.entries[i];
                if (entry.isDirty()) {
                    entry.setDirty(false);
                    if (list == null) {
                        list = new ArrayList<>();
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
    public List<SynchedEntityData.DataValue<?>> getChangedEntries() {
        List<SynchedEntityData.DataValue<?>> list = null;
        for (int i = 0; i < this.entries.length; i++) {
            var entry = this.entries[i];
            if (!entry.isUnchanged()) {
                if (list == null) {
                    list = new ArrayList<>();
                }

                list.add(entry.toSerialized());
            }
        }

        return list;
    }

    public static class Entry<T> {
        final EntityDataAccessor<T> data;
        private final T initialValue;
        T value;
        private boolean dirty;

        public Entry(EntityDataAccessor<T> data, T value) {
            this.data = data;
            this.initialValue = value;
            this.value = value;
        }

        public EntityDataAccessor<T> getData() {
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

        public SynchedEntityData.DataValue<T> toSerialized() {
            return SynchedEntityData.DataValue.create(this.data, this.value);
        }
    }
}