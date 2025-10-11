package eu.pb4.polymer.blocks.impl;

import com.google.common.base.Splitter;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class VanillaBlockPropertiesPredicate {
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SIGN_SPLITTER = Splitter.on('=').limit(2);

    public static <O, S extends State<O, S>> Predicate<State<O, S>> parse(StateManager<O, S> stateManager, String string) {
        var map = new HashMap<Property<?>, Comparable<?>>();
        var parts = COMMA_SPLITTER.split(string).iterator();

        while (true) {
            Iterator<String> keyValIterator;
            do {
                if (!parts.hasNext()) {
                    return (state) -> {
                        var var2 = map.entrySet().iterator();

                        Map.Entry<Property<?>, Comparable<?>> entry;
                        do {
                            if (!var2.hasNext()) {
                                return true;
                            }

                            entry = var2.next();
                        } while (Objects.equals(state.get((Property) entry.getKey()), entry.getValue()));

                        return false;
                    };
                }

                String string2 = parts.next();
                keyValIterator = EQUAL_SIGN_SPLITTER.split(string2).iterator();
            } while (!keyValIterator.hasNext());

            var key = keyValIterator.next();
            var property = stateManager.getProperty(key);
            if (property != null && keyValIterator.hasNext()) {
                String val = keyValIterator.next();
                var comparable = parse(property, val);
                if (comparable == null) {
                    continue;
                }

                map.put(property, comparable);
            }
        }
    }

    @Nullable
    private static <T extends Comparable<T>> T parse(Property<T> property, String value) {
        return property.parse(value).orElse(null);
    }
}