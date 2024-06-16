package eu.pb4.polymer.core.impl;

import net.minecraft.server.network.ServerPlayerEntity;

public interface TransformingComponent {
    Object polymer$getTransformed(ServerPlayerEntity player);
    boolean polymer$requireModification(ServerPlayerEntity player);
}
