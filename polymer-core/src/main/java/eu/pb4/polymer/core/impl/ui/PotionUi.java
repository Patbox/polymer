package eu.pb4.polymer.core.impl.ui;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class PotionUi extends MicroUi {
    private final ServerPlayerEntity player;
    private int tickVal;

    public PotionUi(ServerPlayerEntity player) {
        super(6);
        this.title(Text.literal("Status Effects"));
        this.player = player;
        this.drawUi();

        this.open(player);
    }

    private void drawUi() {
        int id = 0;
        this.clear();
        for (var effectInstance : this.player.getStatusEffects()) {
            if (id == this.size) {
                return;
            }
            ItemStack icon;
            if (effectInstance.getEffectType() instanceof PolymerStatusEffect polymerStatusEffect) {
                icon = polymerStatusEffect.getPolymerIcon(this.player);
                if (icon == null) {
                    continue;
                }
            } else {
                icon = Items.POTION.getDefaultStack();
                icon.getOrCreateNbt().putInt("CustomPotionColor", effectInstance.getEffectType().getColor());
            }
            icon.setCustomName(Text.empty().setStyle(Style.EMPTY.withItalic(false))
                    .append(effectInstance.getEffectType().getName())
                    .append(Text.literal(" (")
                            .append(StatusEffectUtil.durationToString(effectInstance, 1.0F))
                            .append(")")
                            .formatted(Formatting.GRAY))
            );

            icon.getNbt().putInt("HideFlags", 255);
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
