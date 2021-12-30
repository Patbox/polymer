package eu.pb4.polymer.ext.client.impl;

import net.minecraft.util.Identifier;

public class CEImplUtils {
    public static final Identifier id(String path) {
        return new Identifier("polymer_client_ext", path);
    }
}
