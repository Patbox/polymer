package eu.pb4.polymer.core.impl;

import net.minecraft.component.ComponentType;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TransformingDataComponent {
    Object polymer$getTransformed(ServerPlayerEntity player);
    boolean polymer$requireModification(ServerPlayerEntity player);
}
