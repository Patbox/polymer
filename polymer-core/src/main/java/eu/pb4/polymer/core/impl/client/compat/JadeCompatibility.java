package eu.pb4.polymer.core.impl.client.compat;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.api.client.PolymerClientUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import snownee.jade.addon.debug.RegistryNameProvider;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
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

            registrar.addItemModNameCallback(CompatUtils::getModName);
        }
    }

    private static class BlockOverride implements IBlockComponentProvider {
        public static final BlockOverride INSTANCE = new BlockOverride();

        private static final Identifier ID = Identifier.tryParse("polymer:blockstate");

        @Override
        public IElement getIcon(BlockAccessor accessor, snownee.jade.api.config.IPluginConfig config, IElement currentIcon) {
            try {
                var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
                if (block != ClientPolymerBlock.NONE_STATE) {
                    BlockState state = accessor.getLevel().getBlockState(accessor.getPosition());

                    var itemStack = block.block().displayStack();
                    if (itemStack.isEmpty()) {
                        itemStack = state.getBlock().getPickStack(accessor.getLevel(), accessor.getPosition(), state);
                        if (!itemStack.isEmpty() && state.hasBlockEntity()) {
                            var blockEntity = accessor.getLevel().getBlockEntity(accessor.getPosition());

                            if (blockEntity != null) {
                                itemStack.applyComponentsFrom(blockEntity.getComponents());
                            }
                        }
                    }

                    return ItemStackElement.of(itemStack);
                }
            } catch (Throwable e) {

            }
            return null;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            try {

                var block = InternalClientRegistry.getBlockAt(accessor.getPosition());
            if (block != ClientPolymerBlock.NONE_STATE) {
                var formatting = IWailaConfig.get().getFormatting();
                tooltip.clear();
                try {
                    tooltip.add(IThemeHelper.get().title(block.block().name().getString()), Identifiers.CORE_OBJECT_NAME);
                } catch (Throwable e) {
                }
                try {

                    RegistryNameProvider.Mode mode = config.getEnum(Identifiers.DEBUG_REGISTRY_NAME);

                    if (mode != RegistryNameProvider.Mode.OFF) {
                        if (mode != RegistryNameProvider.Mode.ADVANCED_TOOLTIPS || MinecraftClient.getInstance().options.advancedItemTooltips) {
                            tooltip.add(formatting.registryName(block.block().identifier().toString()));
                        }
                    }
                } catch (Throwable e) {
                }

                try {

                    if (config.get(Identifiers.DEBUG_BLOCK_STATES)) {
                        IElementHelper helper = IElementHelper.get();
                        ITooltip box = helper.tooltip();
                        block.states().entrySet().forEach((p) -> {
                            MutableText valueText = Text.literal(" " + p.getValue()).formatted();
                            if (p.getValue().equals("true") || p.getValue().equals("false")) {
                                valueText = valueText.formatted(p.getValue().equals("true") ? Formatting.GREEN : Formatting.RED);
                            }

                            box.add(Text.literal(p.getKey() + ":").append(valueText));
                        });
                        tooltip.add(helper.box(box, BoxStyle.getNestedBox()));
                    }
                } catch (Throwable e) {
                }
                try {

                    if (config.get(Identifiers.CORE_MOD_NAME)) {
                        String modName = ModIdentification.getModName(block.block().identifier());

                        if (modName == null || modName.isEmpty() || modName.equals("Minecraft")) {
                            modName = "Server";
                        }
                        tooltip.add(IThemeHelper.get().modName(modName), Identifiers.CORE_MOD_NAME);
                    }
                } catch (Throwable e) {
                }

            }
            } catch (Throwable e) {

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
            try {

                var entity = accessor.getEntity();

            var type = PolymerClientUtils.getEntityType(entity);

            if (type != null) {
                tooltip.clear();
                if (type != null) {
                    try {

                        tooltip.add(IThemeHelper.get().title(entity.getDisplayName().getString()), Identifiers.CORE_OBJECT_NAME);
                    } catch (Throwable e) {
                    }

                    var formatting = IWailaConfig.get().getFormatting();

                    RegistryNameProvider.Mode mode = config.getEnum(Identifiers.DEBUG_REGISTRY_NAME);
                    try {

                        if (mode != RegistryNameProvider.Mode.OFF) {
                            if (mode != RegistryNameProvider.Mode.ADVANCED_TOOLTIPS || MinecraftClient.getInstance().options.advancedItemTooltips) {
                                tooltip.add(formatting.registryName(type.identifier().toString()));
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
                            tooltip.add(IThemeHelper.get().modName(modName), Identifiers.CORE_MOD_NAME);
                        }
                    } catch (Throwable e) {
                    }

                }
            }
            } catch (Throwable e) {

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