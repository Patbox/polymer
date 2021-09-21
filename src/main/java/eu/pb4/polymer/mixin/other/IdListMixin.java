package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.PolymerUtils;
import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.other.DualList;
import eu.pb4.polymer.other.NetworkIdList;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;


@Mixin(IdList.class)
public class IdListMixin implements NetworkIdList {
    @Shadow @Final private IdentityHashMap<Object, Integer> idMap;
    @Shadow @Mutable private List<Object> list;
    @Unique private int polymer_blockStateId = 0;
    @Unique private boolean polymer_offsetBlockStates;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private <T> void polymer_moveToEnd(T value, int id, CallbackInfo ci) {
        if (this.polymer_offsetBlockStates && value instanceof BlockState blockState && blockState.getBlock() instanceof VirtualBlock) {
            var list = (DualList<Object>) this.list;
            this.idMap.put(value, this.polymer_blockStateId + PolymerUtils.BLOCK_STATE_OFFSET);
            while(list.sizeOffset() <= this.polymer_blockStateId) {
                list.getOffsetList().add(null);
            }

            list.getOffsetList().set(this.polymer_blockStateId, value);
            this.polymer_blockStateId++;
            ci.cancel();
        }
    }

    @Override
    public void polymer_enableOffset() {
        this.polymer_offsetBlockStates = true;
        this.list = new DualList<>((ArrayList<Object>) this.list, new ArrayList<>(), Integer.MAX_VALUE / 2);
    }
}
