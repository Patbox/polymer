package eu.pb4.polymer.core.impl.ui;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.List;
import java.util.Optional;


public class PotionUi extends MicroUi {
    private final ServerPlayer player;
    private int tickVal;

    public PotionUi(ServerPlayer player) {
        super(6);
        this.title(Component.literal("Status Effects"));
        this.player = player;
        this.drawUi();

        this.open(player);
    }

    private void drawUi() {
        int id = 0;
        this.clear();
        for (var effectInstance : this.player.getActiveEffects()) {
            if (id == this.size) {
                return;
            }
            ItemStack icon;
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.MOB_EFFECT, effectInstance.getEffect().value()) instanceof PolymerStatusEffect polymerStatusEffect) {
                icon = polymerStatusEffect.getPolymerIcon(effectInstance.getEffect().value(), this.player);
                if (icon == null) {
                    continue;
                }
            } else {
                icon = Items.POTION.getDefaultInstance();
                icon.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(effectInstance.getEffect().value().getColor()), List.of(), Optional.empty()));
            }
            icon.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.POTION_CONTENTS, true));
            icon.set(DataComponents.RARITY, Rarity.COMMON);
            icon.set(DataComponents.CUSTOM_NAME, Component.empty()
                    .append(effectInstance.getEffect().value().getDisplayName())
                    .append(Component.literal(" (")
                            .append(MobEffectUtil.formatDuration(effectInstance, 1.0F, this.player.level().getServer().tickRateManager().tickrate()))
                            .append(")")
                            .withStyle(ChatFormatting.GRAY))
                    .setStyle(Style.EMPTY.withItalic(false))
            );

            //icon.getNbt().putInt("HideFlags", 255);
            this.slot(id++, icon);
        }
    }

    @Override
    protected void tick() {
        this.tickVal++;

        if (this.tickVal == 20) {
            this.tickVal = 0;
            this.drawUi();
        }
    }
}
