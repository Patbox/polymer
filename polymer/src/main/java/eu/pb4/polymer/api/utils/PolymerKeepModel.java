package eu.pb4.polymer.api.utils;

/**
 * This interface marks blocks and items to keep their model while loaded on client.
 * Can be useful if you want to use companion client side mod
 */
public interface PolymerKeepModel {
    static boolean useServerModel(Object object) {
        return object instanceof PolymerObject && !(object instanceof PolymerKeepModel);
    }

    static boolean useClientModel(Object object) {
        return !(object instanceof PolymerObject) || object instanceof PolymerKeepModel;
    }

    static boolean is(Object object) {
        return object instanceof PolymerKeepModel;
    }
}
