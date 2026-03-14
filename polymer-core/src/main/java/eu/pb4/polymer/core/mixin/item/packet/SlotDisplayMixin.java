package eu.pb4.polymer.core.mixin.item.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.SkipCheck;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.SlotDisplay;

@Mixin(SlotDisplay.class)
public interface SlotDisplayMixin {
    @SuppressWarnings("DataFlowIssue")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> transformDisplays(StreamCodec<RegistryFriendlyByteBuf, SlotDisplay> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, display) -> switch (display) {
            case SlotDisplay.ItemSlotDisplay item when PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, item.item().value()) instanceof PolymerItem ->
                    new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(item.item().value()));
            case SlotDisplay.TagSlotDisplay tagSlot when !((SkipCheck) (Object) tagSlot).polymer$skipped() -> {
                var tag = buf.registryAccess().lookupOrThrow(Registries.ITEM).get(tagSlot.tag());
                if (tag.isEmpty()) {
                    yield tagSlot;
                }

                var array = new ArrayList<SlotDisplay>();
                for (var entry : tag.get()) {
                    if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, entry.value()) instanceof PolymerItem) {
                        array.add(new SlotDisplay.ItemStackSlotDisplay(new ItemStackTemplate(entry.value())));
                    }
                }
                if (!array.isEmpty()) {
                    var out = new SlotDisplay.TagSlotDisplay(tagSlot.tag());
                    ((SkipCheck) (Object) out).polymer$setSkipped();

                    if (CompatStatus.POLYMC) {
                        if (((SkipCheck) (Object) tagSlot).polymc$skipped()) {
                            ((SkipCheck) (Object) out).polymc$setSkipped();
                        }
                    }

                    array.addFirst(out);
                    yield new SlotDisplay.Composite(array);
                }
                yield tagSlot;
            }
            default -> display;
        });
    }
}
