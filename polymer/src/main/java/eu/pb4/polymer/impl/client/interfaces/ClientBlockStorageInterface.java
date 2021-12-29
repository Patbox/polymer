package eu.pb4.polymer.impl.client.interfaces;

import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public interface ClientBlockStorageInterface {
    void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block);
    ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z);

    boolean polymer_hasClientPalette();
}
