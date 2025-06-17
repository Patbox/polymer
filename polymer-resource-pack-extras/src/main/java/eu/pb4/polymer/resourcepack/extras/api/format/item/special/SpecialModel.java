package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.Function;

public interface SpecialModel {
    Codecs.IdMapper<Identifier, MapCodec<? extends SpecialModel>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.ofVanilla("bed"), BedSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("banner"), BannerSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("conduit"), ConduitSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("chest"), ChestSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("head"), HeadSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("player_head"), PlayerHeadSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("shulker_box"), ShulkerBoxSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("shield"), ShieldSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("trident"), TridentSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("decorated_pot"), DecoratedPotSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("standing_sign"), SignSpecialModel.CODEC);
        m.put(Identifier.ofVanilla("hanging_sign"), HangingSignSpecialModel.CODEC);
    });
    Codec<SpecialModel> CODEC = TYPES.getCodec(Identifier.CODEC).dispatch(SpecialModel::codec, Function.identity());
    MapCodec<? extends SpecialModel> codec();
}