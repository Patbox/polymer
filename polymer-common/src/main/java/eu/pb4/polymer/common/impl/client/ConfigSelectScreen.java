package eu.pb4.polymer.common.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigSelectScreen extends Screen {
    private static final Component TITLE = Component.literal("Polymer Configuration\nNote: Some settings only apply after restart.");
    private final Screen parent;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public ConfigSelectScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addToHeader(new MultiLineTextWidget(TITLE, this.font).setCentered(true));
        LinearLayout directionalLayoutWidget = this.layout.addToContents(LinearLayout.vertical()).spacing(8);
        directionalLayoutWidget.defaultCellSetting().alignHorizontallyCenter();
        for (var entry : CommonImpl.KNOWN_CONFIGS) {
            directionalLayoutWidget.addChild(Button.builder(Component.literal(entry.getKey()), (d) -> openConfig(entry.getKey(), entry.getValue())).width(210).build());
        }
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).width(200).build());
        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    private void openConfig(String config, Class<?> clazz) {
        this.minecraft.setScreen(new ConfigEditorScreen(config, clazz, () -> {
            this.minecraft.setScreen(this);
        }));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
