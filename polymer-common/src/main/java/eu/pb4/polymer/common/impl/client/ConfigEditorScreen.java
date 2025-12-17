package eu.pb4.polymer.common.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigEditorScreen extends Screen {
    private static final Component SUB_TITLE = Component.literal("Note: Some settings only apply after restart.");
    private final String config;
    private final Class<?> clazz;
    private final Runnable runnable;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private MultiLineEditBox textField;
    private Button saveAndQuilt = null;
    public ConfigEditorScreen(String config, Class<?> clazz, Runnable object) {
        super(Component.literal(config));
        this.config = config;
        this.clazz = clazz;
        this.runnable = object;
    }

    @Override
    protected void init() {
        var t = new MultiLineTextWidget(Component.literal(config).append("\n").append(SUB_TITLE), this.font).setCentered(true);
        t.setHeight(18);
        this.layout.addToHeader(t);

        var textField = MultiLineEditBox.builder().build(this.font, Math.min(this.width - 40, 512), this.height, Component.empty());
        if (this.textField == null) {
            var cfg = CommonImpl.GSON_PRETTY.toJson(CommonImpl.loadConfig(config, clazz));
            textField.setValue(cfg);
        } else {
            textField.setValue(this.textField.getValue());
        }
        this.textField = textField;
        textField.setValueListener((text) -> {
            try {
                CommonImpl.GSON_PRETTY.fromJson(this.textField.getValue(), this.clazz);
                this.saveAndQuilt.active = true;
            } catch (Throwable e) {
                this.saveAndQuilt.active = false;
            }
        });


        this.layout.addToContents(textField);

        var footer = LinearLayout.horizontal().spacing(8);

        this.saveAndQuilt = footer.addChild(Button.builder(Component.literal("Save and Exit"), (button) -> {
            try {
                var cfg = CommonImpl.GSON_PRETTY.fromJson(this.textField.getValue(), this.clazz);
                CommonImpl.saveConfig(this.config, cfg);
                this.onClose();
            } catch (Throwable e) {
                // Ignored
            }
        }).width(200).build());
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.onClose();
        }).width(200).build());

        this.layout.addToFooter(footer);
        this.layout.arrangeElements();
        this.textField.setHeight(this.layout.getContentHeight());
        this.layout.arrangeElements();

        this.layout.visitWidgets(this::addRenderableWidget);
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
    }
    @Override
    public void onClose() {
        this.runnable.run();
    }
}
