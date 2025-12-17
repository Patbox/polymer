package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ItemModel {
    Codec<ItemModel> CODEC = Codec.lazyInitialized(() -> ItemModel.TYPES.codec(Identifier.CODEC).dispatch(ItemModel::codec, Function.identity()));
    ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.withDefaultNamespace("empty"), EmptyItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("model"), BasicItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("special"), SpecialItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("composite"), CompositeItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("bundle/selected_item"), BundleSelectedItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("range_dispatch"), RangeDispatchItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("select"), SelectItemModel.CODEC);
        m.put(Identifier.withDefaultNamespace("condition"), ConditionItemModel.CODEC);
    });

    MapCodec<? extends ItemModel> codec();
    default ItemModel replaceChildren(Replacer replacer) {
        return this;
    }

    interface Replacer {
        Replacer NO_OP = (a, b) -> b;
        @Nullable
        ItemModel modify(ItemModel parent, ItemModel model);

        @Nullable
        default ItemModel modifyDeep(ItemModel parent, ItemModel model) {
            var newModel = this.modify(parent, model);
            if (newModel == model) {
                return model.replaceChildren(this);
            }
            return newModel;
        }
    }
}
