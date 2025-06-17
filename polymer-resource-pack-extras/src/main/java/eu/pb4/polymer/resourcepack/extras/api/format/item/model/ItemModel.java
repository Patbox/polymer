package eu.pb4.polymer.resourcepack.extras.api.format.item.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.component.type.Consumable;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ItemModel {
    Codec<ItemModel> CODEC = Codec.lazyInitialized(() -> ItemModel.TYPES.getCodec(Identifier.CODEC).dispatch(ItemModel::codec, Function.identity()));
    Codecs.IdMapper<Identifier, MapCodec<? extends ItemModel>> TYPES = Util.make(new Codecs.IdMapper<>(), m -> {
        m.put(Identifier.ofVanilla("empty"), EmptyItemModel.CODEC);
        m.put(Identifier.ofVanilla("model"), BasicItemModel.CODEC);
        m.put(Identifier.ofVanilla("special"), SpecialItemModel.CODEC);
        m.put(Identifier.ofVanilla("composite"), CompositeItemModel.CODEC);
        m.put(Identifier.ofVanilla("bundle/selected_item"), BundleSelectedItemModel.CODEC);
        m.put(Identifier.ofVanilla("range_dispatch"), RangeDispatchItemModel.CODEC);
        m.put(Identifier.ofVanilla("select"), SelectItemModel.CODEC);
        m.put(Identifier.ofVanilla("condition"), ConditionItemModel.CODEC);
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
