package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;

public interface TempPlayerLoginAttachments {
    void polymer_setWorldReload(boolean value);
    boolean polymer_getWorldReload();

    PolymerHandshakeHandler polymer_getAndRemoveHandshakeHandler();
    PolymerHandshakeHandler polymer_getHandshakeHandler();

    void polymer_setHandshakeHandler(PolymerHandshakeHandler handler);
}
