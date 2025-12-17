package eu.pb4.polymer.core.api.utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.IdMap;
import net.minecraft.resources.Identifier;

@ApiStatus.NonExtendable
public interface PolymerRegistry<T> extends IdMap<T> {
    @Nullable
    T get(Identifier identifier);

    @Nullable
    T byId(int id);

    @Nullable
    T getDirect(Identifier identifier);

    @Nullable
    Identifier getEntryId(T entry);
    @Override
    int getId(T entry);
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
