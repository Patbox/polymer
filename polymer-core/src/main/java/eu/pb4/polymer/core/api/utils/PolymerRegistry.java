package eu.pb4.polymer.core.api.utils;

import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ApiStatus.NonExtendable
public interface PolymerRegistry<T> extends IndexedIterable<T> {
    @Nullable
    T get(Identifier identifier);

    @Nullable
    T get(int id);

    @Nullable
    T getDirect(Identifier identifier);

    @Nullable
    Identifier getId(T entry);
    int getRawId(T entry);
    Iterable<Identifier> ids();
    Iterable<Map.Entry<Identifier, T>> entries();

    Set<T> getTag(Identifier tag);
    Collection<Identifier> getTags();
    Collection<Identifier> getTagsOf(T entry);
    int size();

    boolean contains(Identifier id);
    boolean containsEntry(T entry);

    Stream<T> stream();
}
