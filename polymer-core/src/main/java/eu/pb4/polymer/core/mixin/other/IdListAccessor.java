package eu.pb4.polymer.core.mixin.other;

import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(IdList.class)
public interface IdListAccessor {
    @Accessor
    List<?> getList();
}
