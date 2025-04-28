package eu.pb4.polymer.rsm.impl;

import net.minecraft.util.Identifier;

public class RegSyncImplUtil {
    public static boolean isVanillaId(Identifier id) {
        return id.getNamespace().equals("minecraft") || id.getNamespace().equals("brigadier");
    }
}
