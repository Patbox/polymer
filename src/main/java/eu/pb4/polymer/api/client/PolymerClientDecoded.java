package eu.pb4.polymer.api.client;

/**
 * This interface marks blocks and items to be decoded on client.
 * Can be useful if you want to use companion client side mod
 */
public interface PolymerClientDecoded {
    default boolean shouldDecodePolymer() {
        return true;
    }

    static boolean checkDecode(Object obj) {
        return obj instanceof PolymerClientDecoded polymerClientDecoded && polymerClientDecoded.shouldDecodePolymer();
    }
}
