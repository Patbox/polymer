package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.api.utils.PolymerRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.IdMapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

@Environment(EnvType.CLIENT)
public interface ClientPolymerRegistries {
    PolymerRegistry<ClientPolymerBlock> BLOCKS = InternalClientRegistry.BLOCKS;
    IdMapper<ClientPolymerBlock.State> BLOCK_STATES = InternalClientRegistry.BLOCK_STATES;
    PolymerRegistry<ClientPolymerItem> ITEMS = InternalClientRegistry.ITEMS;
    PolymerRegistry<ClientPolymerEntityType> ENTITY_TYPES = InternalClientRegistry.ENTITY_TYPES;
    PolymerRegistry<ClientPolymerEntry<VillagerProfession>> VILLAGER_PROFESSIONS = InternalClientRegistry.VILLAGER_PROFESSIONS;
    PolymerRegistry<ClientPolymerEntry<BlockEntityType<?>>> BLOCK_ENTITY = InternalClientRegistry.BLOCK_ENTITY;
    PolymerRegistry<ClientPolymerEntry<MobEffect>> STATUS_EFFECT = InternalClientRegistry.STATUS_EFFECT;
    PolymerRegistry<ClientPolymerEntry<Fluid>> FLUID = InternalClientRegistry.FLUID;
    PolymerRegistry<ClientPolymerEntry<MenuType<?>>> SCREEN_HANDLER = InternalClientRegistry.SCREEN_HANDLER;
    PolymerRegistry<ClientPolymerEntry<DataComponentType<?>>> DATA_COMPONENT_TYPE = InternalClientRegistry.DATA_COMPONENT_TYPE;
    PolymerRegistry<ClientPolymerEntry<DataComponentType<?>>> ENCHANTMENT_COMPONENT_TYPE = InternalClientRegistry.ENCHANTMENT_COMPONENT_TYPE;
}
