package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.HumanoidArm;

public record MainHandProperty() implements SelectProperty<HumanoidArm> {
    public static final Type<MainHandProperty, HumanoidArm> TYPE = new Type<>(MapCodec.unit(new MainHandProperty()), HumanoidArm.CODEC);

    @Override
    public Type<MainHandProperty, HumanoidArm> type() {
        return TYPE;
    }
}
