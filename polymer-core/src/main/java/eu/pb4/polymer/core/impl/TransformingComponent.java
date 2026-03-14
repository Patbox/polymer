package eu.pb4.polymer.core.impl;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public interface TransformingComponent {
    Object polymer$getTransformed(PacketContext context);
    boolean polymer$requireModification(PacketContext context);
}
