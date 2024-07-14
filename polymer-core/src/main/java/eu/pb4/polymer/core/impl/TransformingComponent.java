package eu.pb4.polymer.core.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

public interface TransformingComponent {
    Object polymer$getTransformed(PacketContext context);
    boolean polymer$requireModification(PacketContext context);
}
