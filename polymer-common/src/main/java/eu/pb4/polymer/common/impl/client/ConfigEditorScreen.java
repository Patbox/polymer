package eu.pb4.polymer.common.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigEditorScreen extends Screen {
    private static final Text SUB_TITLE = Text.literal("Note: Some settings only apply after restart.");
    private final String config;
    private final Class<?> clazz;
    private final Runnable runnable;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private EditBoxWidget textField;
    private ButtonWidget saveAndQuilt = null;
    public ConfigEditorScreen(String config, Class<?> clazz, Runnable object) {
        super(Text.literal(config));
        this.config = config;
        this.clazz = clazz;
        this.runnable = object;
    }

    @Override
    protected void init() {
        var t = new MultilineTextWidget(Text.literal(config).append("\n").append(SUB_TITLE), this.textRenderer).setCentered(true);
        t.setHeight(18);
        this.layout.addHeader(t);

        var textField = EditBoxWidget.builder().build(this.textRenderer, Math.min(this.width - 40, 512), this.height, Text.empty());
        if (this.textField == null) {
            var cfg = CommonImpl.GSON_PRETTY.toJson(CommonImpl.loadConfig(config, clazz));
            textField.setText(cfg);
        } else {
            textField.setText(this.textField.getText());
        }
        this.textField = textField;
        textField.setChangeListener((text) -> {
            try {
                CommonImpl.GSON_PRETTY.fromJson(this.textField.getText(), this.clazz);
                this.saveAndQuilt.active = true;
            } catch (Throwable e) {
                this.saveAndQuilt.active = false;
            }
        });


        this.layout.addBody(textField);

        var footer = DirectionalLayoutWidget.horizontal().spacing(8);

        this.saveAndQuilt = footer.add(ButtonWidget.builder(Text.literal("Save and Exit"), (button) -> {
            try {
                var cfg = CommonImpl.GSON_PRETTY.fromJson(this.textField.getText(), this.clazz);
                CommonImpl.saveConfig(this.config, cfg);
                this.close();
            } catch (Throwable e) {
                // Ignored
            }
        }).width(200).build());
        footer.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.close();
        }).width(200).build());

        this.layout.addFooter(footer);
        this.layout.refreshPositions();
        this.textField.setHeight(this.layout.getContentHeight());
        this.layout.refreshPositions();

        this.layout.forEachChild(this::addDrawableChild);
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }
    @Override
    public void close() {
        this.runnable.run();
    }
}
