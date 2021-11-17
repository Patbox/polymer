package eu.pb4.polymer.mixin.client.item.packet;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
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
    @Shadow @Final private TradeOfferList recipes;
    @Unique private TradeOfferList polymer_trades = null;

    @Environment(EnvType.CLIENT)
    @Inject(method = "getOffers", at = @At("HEAD"), cancellable = true)
    private void polymer_replaceRecipes(CallbackInfoReturnable<TradeOfferList> cir) {
        ServerPlayerEntity player = ClientUtils.getPlayer();

        if (player != null) {
            if (this.polymer_trades == null) {
                TradeOfferList list = new TradeOfferList();

                for (TradeOffer tradeOffer : this.recipes) {
                    list.add(new TradeOffer(
                            PolymerItemUtils.getPolymerItemStack(tradeOffer.getOriginalFirstBuyItem(), player),
                            PolymerItemUtils.getPolymerItemStack(tradeOffer.getSecondBuyItem(), player),
                            PolymerItemUtils.getPolymerItemStack(tradeOffer.getSellItem(), player),
                            tradeOffer.getUses(),
                            tradeOffer.getMaxUses(),
                            tradeOffer.getMerchantExperience(),
                            tradeOffer.getPriceMultiplier(),
                            tradeOffer.getDemandBonus()
                    ));
                }

                this.polymer_trades = list;
            }

            cir.setReturnValue(this.polymer_trades);
        }
    }
}
