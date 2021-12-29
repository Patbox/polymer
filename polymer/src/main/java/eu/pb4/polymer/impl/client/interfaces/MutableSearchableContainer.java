package eu.pb4.polymer.impl.client.interfaces;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
@SuppressWarnings({"unused"})
public interface MutableSearchableContainer {
    void polymer_remove(Object obj);
    void polymer_removeIf(Predicate<Object> predicate);
}
