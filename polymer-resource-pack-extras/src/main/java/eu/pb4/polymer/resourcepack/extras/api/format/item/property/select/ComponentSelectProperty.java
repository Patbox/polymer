package eu.pb4.polymer.resourcepack.extras.api.format.item.property.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.SelectItemModel;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;

public record ComponentSelectProperty<T>(ComponentType<T> componentType) implements SelectProperty<T> {
    private static final SelectProperty.Type<? extends ComponentSelectProperty<?>, ?> TYPE = createType();

    private static <T> SelectProperty.Type<ComponentSelectProperty<T>, T> createType() {
        //noinspection unchecked
        var codec = (Codec<ComponentType<T>>) (Object) Registries.DATA_COMPONENT_TYPE.getCodec().validate((componentType) -> {
            return componentType.shouldSkipSerialization() ? DataResult.error(() -> {
                return "Component can't be serialized";
            }) : DataResult.success(componentType);
        });
        var mapCodec = codec.dispatchMap("component", (unbakedSwitch) -> {
            return unbakedSwitch.property().componentType;
        }, (componentType) -> {
            return Type.createCaseListCodec(componentType.getCodecOrThrow()).xmap((cases) -> {
                //noinspection unchecked
                return new SelectItemModel.Switch<>(new ComponentSelectProperty<>(componentType), cases);
            }, SelectItemModel.Switch::cases);

        });
        return new SelectProperty.Type<>(mapCodec);
    }

    public static <T> SelectProperty.Type<ComponentSelectProperty<T>, T> getTypeInstance() {
        //noinspection unchecked
        return (Type<ComponentSelectProperty<T>, T>) TYPE;
    }

    @Override
    public SelectProperty.Type<ComponentSelectProperty<T>, T> type() {
        return getTypeInstance();
    }
}
