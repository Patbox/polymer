package eu.pb4.polymer.impl.client.world;

import eu.pb4.polymer.api.client.block.ClientPolymerBlock;

public interface ClientBlockStorageInterface {
    void polymer_setClientPolymerBlock(int x, int y, int z, ClientPolymerBlock.State block);
    ClientPolymerBlock.State polymer_getClientPolymerBlock(int x, int y, int z);
}
