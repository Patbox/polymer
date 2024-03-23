package eu.pb4.polymer.networking.impl;

import com.mojang.serialization.DataResult;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public class NetImpl {
    public static final boolean SEND_GAME_JOIN_PACKET;
    public static final Logger LOGGER = CommonImpl.LOGGER;
    public static final boolean IS_DISABLED = false;

    static {
        var config = CommonImpl.loadConfig("networking", NetConfig.class);

        SEND_GAME_JOIN_PACKET = config.sendGameJoinBeforeSync;
    }
}
