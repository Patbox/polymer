package eu.pb4.polymer.virtualentity.impl;

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;

public class VirtualEntityImplUtils {
    public static <T> T createUnsafe(Class<T> tClass) {
        try {
            return (T) UnsafeAccess.UNSAFE.allocateInstance(tClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
