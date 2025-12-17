package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.ItemTintSource;
import java.util.List;
import net.minecraft.resources.Identifier;

public record BasicItemModel(Identifier model, List<ItemTintSource> tints) implements ItemModel {
    public static final MapCodec<BasicItemModel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(BasicItemModel::model),
            ItemTintSource.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(BasicItemModel::tints)
    ).apply(instance, BasicItemModel::new));

    public BasicItemModel(Identifier model) {
        this(model, List.of());
    }

    @Override
    public MapCodec<? extends ItemModel> codec() {
        return CODEC;
    }
}
