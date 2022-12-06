package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.SkullItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import snownee.jade.addon.core.RegistryNameProvider;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.util.ModIdentification;

@ApiStatus.Internal
@SuppressWarnings("UnstableApiUsage")
public class JadeCompatibility implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        if (PolymerImpl.IS_CLIENT) {
            registrar.registerBlockComponent(BlockOverride.INSTANCE, Block.class);
            registrar.registerEntityComponent(EntityOverride.INSTANCE, Entity.class);
        }
    }

    private static class BlockOverride implements IBlockComponentProvider {
        public static final BlockOverride INSTANCE = new BlockOverride();

        private static final Identifier ID = Identifier.tryParse("polymer:blockstate");

        @Override
        public IElement getIcon(BlockAccessor accessor, snownee.jade.api.config.IPluginConfig config, IElement currentIcon) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                BlockState state = accessor.getLevel().getBlockState(accessor.getPosition());

                var itemStack = state.getBlock().getPickStack(accessor.getLevel(), accessor.getPosition(), state);

                if (!itemStack.isEmpty() && state.hasBlockEntity() && itemStack.getItem() instanceof SkullItem) {
                    var blockEntity = accessor.getLevel().getBlockEntity(accessor.getPosition());

                    if (blockEntity != null) {
                        var nbtCompound = blockEntity.createNbt();
                        if (nbtCompound.contains("SkullOwner")) {
                            itemStack.getOrCreateNbt().put("SkullOwner", nbtCompound.getCompound("SkullOwner"));
                        }
                    }
                }

                return ItemStackElement.of(itemStack);
            }
            return null;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                var formatting = config.getWailaConfig().getFormatting();
                tooltip.clear();
                try {
                    tooltip.add(formatting.title(block.block().name().getString()), Identifiers.CORE_OBJECT_NAME);
                } catch (Throwable e) {
                }
                try {

                    RegistryNameProvider.Mode mode = config.getEnum(Identifiers.CORE_REGISTRY_NAME);

                    if (mode != RegistryNameProvider.Mode.OFF) {
                        if (mode != RegistryNameProvider.Mode.ADVANCED_TOOLTIPS || MinecraftClient.getInstance().options.advancedItemTooltips) {
                            tooltip.add(config.getWailaConfig().getFormatting().registryName(block.block().identifier().toString()));
                        }
                    }
                } catch (Throwable e) {
                }

                try {

                    if (config.get(Identifiers.MC_BLOCK_STATES)) {
                        IElementHelper helper = tooltip.getElementHelper();
                        ITooltip box = helper.tooltip();
                        block.states().entrySet().forEach((p) -> {
                            MutableText valueText = Text.literal(" " + p.getValue()).formatted();
                            if (p.getValue().equals("true") || p.getValue().equals("false")) {
                                valueText = valueText.formatted(p.getValue().equals("true") ? Formatting.GREEN : Formatting.RED);
                            }

                            box.add(Text.literal(p.getKey() + ":").append(valueText));
                        });
                        tooltip.add(helper.box(box));
                    }
                } catch (Throwable e) {
                }
                try {

                    if (config.get(Identifiers.CORE_MOD_NAME)) {
                        String modName = ModIdentification.getModName(block.block().identifier());

                        if (modName == null || modName.isEmpty() || modName.equals("Minecraft")) {
                            modName = "Server";
                        }
                        tooltip.add(Text.literal(String.format(formatting.getModName(), modName)), Identifiers.CORE_MOD_NAME);
                    }
                } catch (Throwable e) {
                }

            }
        }

        @Override
        public Identifier getUid() {
            return ID;
        }

        @Override
        public int getDefaultPriority() {
            return 99999;
        }

        @Override
        public boolean isRequired() {
            return true;
        }
    }

    private static final class EntityOverride implements IEntityComponentProvider {
        public static final EntityOverride INSTANCE = new EntityOverride();
        private static final Identifier ID = Identifier.tryParse("polymer:entities");


        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            var entity = accessor.getEntity();

            var type = PolymerClientUtils.getEntityType(entity);

            if (type != null) {
                tooltip.clear();
                if (type != null) {
                    var formatting = config.getWailaConfig().getFormatting();
                    try {

                        tooltip.add(formatting.title(entity.getDisplayName().getString()), Identifiers.CORE_OBJECT_NAME);
                    } catch (Throwable e) {
                    }


                    RegistryNameProvider.Mode mode = config.getEnum(Identifiers.CORE_REGISTRY_NAME);
                    try {

                        if (mode != RegistryNameProvider.Mode.OFF) {
                            if (mode != RegistryNameProvider.Mode.ADVANCED_TOOLTIPS || MinecraftClient.getInstance().options.advancedItemTooltips) {
                                tooltip.add(config.getWailaConfig().getFormatting().registryName(type.identifier().toString()));
                            }
                        }
                    } catch (Throwable e) {
                    }
                    try {
                        if (config.get(Identifiers.CORE_MOD_NAME)) {
                            String modName = ModIdentification.getModName(type.identifier());

                            if (modName == null || modName.isEmpty() || modName.equals("Minecraft")) {
                                modName = "Server";
                            }
                            tooltip.add(Text.literal(String.format(formatting.getModName(), modName)), Identifiers.CORE_MOD_NAME);
                        }
                    } catch (Throwable e) {
                    }

                }
            }
        }

        @Override
        public Identifier getUid() {
            return ID;
        }

        @Override
        public int getDefaultPriority() {
            return 999999;
        }

        @Override
        public boolean isRequired() {
            return true;
        }
    }
}