package eu.pb4.polymer.core.impl;

import net.minecraft.component.DataComponentType;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TransformingDataComponent {
    Object polymer$getTransformed(ServerPlayerEntity player);
    boolean polymer$requireModification(ServerPlayerEntity player);
}
