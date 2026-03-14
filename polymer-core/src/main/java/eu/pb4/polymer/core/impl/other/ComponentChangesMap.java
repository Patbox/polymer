package eu.pb4.polymer.core.impl.other;

import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public record ComponentChangesMap(DataComponentPatch changes) implements DataComponentMap {
    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> type) {
        var x = this.changes.get(DataComponentMap.EMPTY, type);
        //noinspection OptionalAssignedToNull
        return x;
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        var set = new HashSet<DataComponentType<?>>();
        for (var entry : this.changes.entrySet()) {
            if (entry.getValue().isPresent()) {
                set.add(entry.getKey());
            }
        }
        return set;
    }
}
