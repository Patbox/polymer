package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.rsm.impl.CompatStatus;

public class NetImpl {
    public static final boolean SEND_GAME_JOIN_PACKET;

    static {
        var config = PolymerImpl.loadConfig("networking", NetConfig.class);

        SEND_GAME_JOIN_PACKET = config.sendGameJoinBeforeSync || CompatStatus.PROXY_MODS;
    }
}
