package eu.pb4.polymer.virtualentity.api.data;

import eu.pb4.polymer.common.mixin.SyncedEntityDataAccessor;
import org.jspecify.annotations.Nullable;

import java.util.List;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public interface SynchedEntityDataLike {
    @Nullable
    <T> T get(EntityDataAccessor<T> data);

    default <T> void set(EntityDataAccessor<T> key, T value) {
        set(key, value, false);
    }

    <T> void set(EntityDataAccessor<T> key, T value, boolean forceDirty);

    <T> void setDirty(EntityDataAccessor<T> key, boolean isDirty);

    boolean isDirty();

    boolean isDirty(EntityDataAccessor<?> key);

    @Nullable
    List<SynchedEntityData.DataValue<?>> getDirtyEntries();

    @Nullable
    List<SynchedEntityData.DataValue<?>> getChangedEntries();

    static SynchedEntityDataLike wrap(SynchedEntityData dataTracker) {
        return new SynchedEntityDataLike() {
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
                return ((SyncedEntityDataAccessor) dataTracker).getItemsById()[key.id()].isDirty();
            }

            @Override
            public @Nullable List<SynchedEntityData.DataValue<?>> getDirtyEntries() {
                return dataTracker.packDirty();
            }

            @Override
            public @Nullable List<SynchedEntityData.DataValue<?>> getChangedEntries() {
                return dataTracker.getNonDefaultValues();
            }
        };
    }
}