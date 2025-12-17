package eu.pb4.polymer.virtualentity.api.tracker;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class WrappingDataTracker implements DataTrackerLike {
    private final DataTrackerLike dataTracker;

    public WrappingDataTracker(DataTrackerLike tracker) {
        this.dataTracker = tracker;
    }
    
    @Override
    public <T> @Nullable T get(EntityDataAccessor<T> data) {
        return dataTracker.get(data);
    }

    @Override
    public <T> void set(EntityDataAccessor<T> key, T value, boolean forceDirty) {
        dataTracker.set(key, value, forceDirty);
    }

    @Override
    public <T> void setDirty(EntityDataAccessor<T> key, boolean isDirty) {
        dataTracker.set(key, dataTracker.get(key), isDirty);
    }

    @Override
    public boolean isDirty() {
        return dataTracker.isDirty();
    }

    @Override
    public boolean isDirty(EntityDataAccessor<?> key) {
        return dataTracker.isDirty(key);
    }

    @Override
    public @Nullable List<SynchedEntityData.DataValue<?>> getDirtyEntries() {
        return dataTracker.getDirtyEntries();
    }

    @Override
    public @Nullable List<SynchedEntityData.DataValue<?>> getChangedEntries() {
        return dataTracker.getChangedEntries();
    }
}
