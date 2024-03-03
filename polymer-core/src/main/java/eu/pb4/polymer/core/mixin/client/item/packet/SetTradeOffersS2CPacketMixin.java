package eu.pb4.polymer.core.mixin.client.item.packet;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(SetTradeOffersS2CPacket.class)
public class SetTradeOffersS2CPacketMixin {
    @Shadow @Final private TradeOfferList offers;
    @Unique private TradeOfferList polymer$trades = null;

    @Environment(EnvType.CLIENT)
    @Inject(method = "getOffers", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceOffers(CallbackInfoReturnable<TradeOfferList> cir) {
        ServerPlayerEntity player = ClientUtils.getPlayer();

        if (player != null) {
            if (this.polymer$trades == null) {
                TradeOfferList list = new TradeOfferList();

                for (TradeOffer tradeOffer : this.offers) {
                    var s1 = PolymerItemUtils.getPolymerItemStack(tradeOffer.getOriginalFirstBuyItem(), player);

                    var offer = new TradeOffer(
                            new TradedItem(Registries.ITEM.getEntry(s1.getItem()), s1.getCount(), ComponentPredicate.EMPTY, s1),
                            tradeOffer.getSecondBuyItem().map(stack -> {
                                var s = PolymerItemUtils.getPolymerItemStack(stack.itemStack(), player);
                                return new TradedItem(Registries.ITEM.getEntry(s.getItem()), s.getCount(), stack.components(), s);
                            }),
                            PolymerItemUtils.getPolymerItemStack(tradeOffer.getSellItem(), player),
                            tradeOffer.getUses(),
                            tradeOffer.getMaxUses(),
                            tradeOffer.getMerchantExperience(),
                            tradeOffer.getPriceMultiplier(),
                            tradeOffer.getDemandBonus()
                    );
                    offer.setSpecialPrice(tradeOffer.getSpecialPrice());
                    list.add(offer);
                }

                this.polymer$trades = list;
            }

            cir.setReturnValue(this.polymer$trades);
        }
    }
}
