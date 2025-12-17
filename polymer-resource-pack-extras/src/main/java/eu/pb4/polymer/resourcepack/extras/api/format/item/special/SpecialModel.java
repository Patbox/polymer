package eu.pb4.polymer.resourcepack.extras.api.format.item.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.common.impl.LazyIdMapper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import java.util.function.Function;

public interface SpecialModel {
    ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModel>> TYPES = new LazyIdMapper<>(m -> {
        m.put(Identifier.withDefaultNamespace("bed"), BedSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("banner"), BannerSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("conduit"), ConduitSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("copper_golem_statue"), CopperGolemStatueSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("chest"), ChestSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("head"), HeadSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("player_head"), PlayerHeadSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("shulker_box"), ShulkerBoxSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("shield"), ShieldSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("trident"), TridentSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("decorated_pot"), DecoratedPotSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("standing_sign"), SignSpecialModel.CODEC);
        m.put(Identifier.withDefaultNamespace("hanging_sign"), HangingSignSpecialModel.CODEC);
    });
    Codec<SpecialModel> CODEC = TYPES.codec(Identifier.CODEC).dispatch(SpecialModel::codec, Function.identity());
    MapCodec<? extends SpecialModel> codec();
}