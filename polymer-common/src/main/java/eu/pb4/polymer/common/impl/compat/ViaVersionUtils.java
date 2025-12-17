package eu.pb4.polymer.common.impl.compat;

import com.viaversion.viaversion.api.Via;
import net.minecraft.SharedConstants;

import java.util.UUID;

public class ViaVersionUtils {
    public static int getProtocol(UUID uuid) {
        var x = Via.getAPI().getPlayerProtocolVersion(uuid);
        if (x != null) {
            return x.getVersion();
        }
        return SharedConstants.getCurrentVersion().protocolVersion();
    }
}
