package eu.pb4.polymer.common.impl.client;

import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigSelectScreen extends Screen {
    private static final Text TITLE = Text.literal("Polymer Configuration\nNote: Some settings only apply after restart.");
    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public ConfigSelectScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.layout.addHeader(new MultilineTextWidget(TITLE, this.textRenderer).setCentered(true));
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addBody(DirectionalLayoutWidget.vertical()).spacing(8);
        directionalLayoutWidget.getMainPositioner().alignHorizontalCenter();
        for (var entry : CommonImpl.KNOWN_CONFIGS) {
            directionalLayoutWidget.add(ButtonWidget.builder(Text.literal(entry.getKey()), (d) -> openConfig(entry.getKey(), entry.getValue())).width(210).build());
        }
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).width(200).build());
        this.layout.refreshPositions();
        this.layout.forEachChild(this::addDrawableChild);
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    private void openConfig(String config, Class<?> clazz) {
        this.client.setScreen(new ConfigEditorScreen(config, clazz, () -> {
            this.client.setScreen(this);
        }));
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
