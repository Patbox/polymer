package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NetImpl {
    public static final boolean SEND_GAME_JOIN_PACKET;

    static {
        var config = CommonImpl.loadConfig("networking", NetConfig.class);

        SEND_GAME_JOIN_PACKET = config.sendGameJoinBeforeSync || CompatStatus.PROXY_MODS;
    }
}
