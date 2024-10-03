package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.impl.interfaces.SkipCheck;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(SlotDisplay.class)
public interface SlotDisplayMixin {
    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, SlotDisplay> transformDisplays(PacketCodec<RegistryByteBuf, SlotDisplay> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, display) -> switch (display) {
            case SlotDisplay.ItemSlotDisplay item when item.item().value() instanceof PolymerItem ->
                    new SlotDisplay.StackSlotDisplay(item.item().value().getDefaultStack());
            case SlotDisplay.TagSlotDisplay tagSlot when !((SkipCheck) (Object) tagSlot).polymer$skipped() -> {
                var tag = buf.getRegistryManager().getOrThrow(RegistryKeys.ITEM).getOptional(tagSlot.tag());
                if (tag.isEmpty()) {
                    yield tagSlot;
                }

                var array = new ArrayList<SlotDisplay>();
                for (var entry : tag.get()) {
                    if (entry.value() instanceof PolymerItem) {
                        array.add(new SlotDisplay.StackSlotDisplay(entry.value().getDefaultStack()));
                    }
                }
                if (!array.isEmpty()) {
                    var out = new SlotDisplay.TagSlotDisplay(tagSlot.tag());
                    ((SkipCheck) (Object) out).polymer$setSkipped();
                    array.addFirst(out);
                    yield new SlotDisplay.CompositeSlotDisplay(array);
                }
                yield tagSlot;
            }
            default -> display;
        });
    }
}
