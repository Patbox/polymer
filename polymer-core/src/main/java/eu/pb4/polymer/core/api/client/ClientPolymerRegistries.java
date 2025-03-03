package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.IdList;
import net.minecraft.village.VillagerProfession;

@Environment(EnvType.CLIENT)
public interface ClientPolymerRegistries {
    PolymerRegistry<ClientPolymerBlock> BLOCKS = InternalClientRegistry.BLOCKS;
    IdList<ClientPolymerBlock.State> BLOCK_STATES = InternalClientRegistry.BLOCK_STATES;
    PolymerRegistry<ClientPolymerItem> ITEMS = InternalClientRegistry.ITEMS;
    PolymerRegistry<ClientPolymerEntityType> ENTITY_TYPES = InternalClientRegistry.ENTITY_TYPES;
    PolymerRegistry<ClientPolymerEntry<VillagerProfession>> VILLAGER_PROFESSIONS = InternalClientRegistry.VILLAGER_PROFESSIONS;
    PolymerRegistry<ClientPolymerEntry<BlockEntityType<?>>> BLOCK_ENTITY = InternalClientRegistry.BLOCK_ENTITY;
    PolymerRegistry<ClientPolymerEntry<StatusEffect>> STATUS_EFFECT = InternalClientRegistry.STATUS_EFFECT;
    PolymerRegistry<ClientPolymerEntry<Fluid>> FLUID = InternalClientRegistry.FLUID;
    PolymerRegistry<ClientPolymerEntry<ScreenHandlerType<?>>> SCREEN_HANDLER = InternalClientRegistry.SCREEN_HANDLER;
    PolymerRegistry<ClientPolymerEntry<ComponentType<?>>> DATA_COMPONENT_TYPE = InternalClientRegistry.DATA_COMPONENT_TYPE;
    PolymerRegistry<ClientPolymerEntry<ComponentType<?>>> ENCHANTMENT_COMPONENT_TYPE = InternalClientRegistry.ENCHANTMENT_COMPONENT_TYPE;
}
