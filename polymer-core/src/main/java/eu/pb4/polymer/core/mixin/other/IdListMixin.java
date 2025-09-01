package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;


@Mixin(IdList.class)
public abstract class IdListMixin<T> implements PolymerIdList<T> {
    @Final
    @Shadow
    @Mutable
    private List<T> list;
    @Shadow
    private int nextId;
    @Shadow
    @Final
    private Reference2IntMap<T> idMap;
    @Unique
    private int polymer$nonPolymerBitCount;
    @Unique
    private int polymer$vanillaBitCount;
    @Unique
    private int polymer$vanillaEntryCount;

    @Shadow
    public abstract void add(T value);

    @Shadow public abstract int size();

    @Unique
    private boolean polymer$requireReorder = true;

    @Unique
    private final Set<T> polymer$states = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
    @Unique
    private int polymer$offset = Integer.MAX_VALUE;
    @Unique
    private Predicate<T> polymer$polymerEntryChecker;
    @Unique
    private Predicate<T> polymer$serverEntryChecker;
    @Unique
    private boolean polymer$isPolymerAware;
    @Unique
    private Function<T, String> polymer$nameCreator;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void polymer$moveToEnd(T value, CallbackInfo ci) {
        this.polymer$requireReorder = true;
    }

    @Override
    public Collection<T> polymer$getPolymerEntries() {
        return this.polymer$states;
    }

    @Override
    public void polymer$reorderEntries() {
        if (!this.polymer$isPolymerAware || !this.polymer$requireReorder) {
            return;
        }
        this.polymer$vanillaEntryCount = 0;
        this.polymer$states.clear();

        var untouched = new ArrayList<T>();
        var polymer = new ArrayList<T>();
        for (var value : this.list) {
            var isPolymerObj = this.polymer$polymerEntryChecker.test(value);

            if (isPolymerObj) {
                polymer.add(value);
            } else {
                untouched.add(value);
            }

            if (!isPolymerObj && !this.polymer$serverEntryChecker.test(value)) {
                this.polymer$vanillaEntryCount++;
            } else {
                this.polymer$states.add(value);

            }
        }

        this.list.clear();
        this.list.addAll(untouched);
        this.list.addAll(polymer);

        this.idMap.clear();
        for (int i = 0; i < this.list.size(); i++) {
            this.idMap.put(this.list.get(i), i);
        }


        this.polymer$offset = this.list.size() - this.polymer$states.size();
        this.polymer$nonPolymerBitCount = MathHelper.ceilLog2(this.list.size() - this.polymer$states.size());
        this.polymer$vanillaBitCount = MathHelper.ceilLog2(this.polymer$vanillaEntryCount);
        this.polymer$requireReorder = false;
    }

    @Override
    public int polymer$getNonPolymerBitCount() {
        return this.polymer$nonPolymerBitCount;
    }

    @Override
    public int polymer$getVanillaBitCount() {
        return this.polymer$vanillaBitCount;
    }

    @Override
    public void polymer$setChecker(Predicate<T> polymerChecker, Predicate<T> serverChecker,  Function<T, String> namer) {
        this.polymer$polymerEntryChecker = polymerChecker;
        this.polymer$serverEntryChecker = serverChecker;
        this.polymer$isPolymerAware = polymerChecker != null;
        this.polymer$nameCreator = namer;
    }

    @Override
    public int polymer$getOffset() {
        return this.polymer$offset;
    }

    @Override
    public void polymer$clear() {
        this.nextId = 0;
        this.idMap.clear();
        this.list.clear();
        this.polymer$vanillaEntryCount = 0;
        this.polymer$states.clear();
        this.polymer$offset = Integer.MAX_VALUE;
    }
}
