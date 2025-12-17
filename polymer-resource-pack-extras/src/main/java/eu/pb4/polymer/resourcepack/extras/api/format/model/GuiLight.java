package eu.pb4.polymer.resourcepack.extras.api.format.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum GuiLight implements StringRepresentable {
    SIDE("side"),
    FRONT("front");

    public static final Codec<GuiLight> CODEC = StringRepresentable.fromEnum(GuiLight::values);

    private final String name;
    private GuiLight(String name) {
        this.name = name;
    }
    @Override
    public String getSerializedName() {
        return this.name;
    }
}
