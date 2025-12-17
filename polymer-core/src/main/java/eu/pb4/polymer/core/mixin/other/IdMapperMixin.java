package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.IdMapper;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;


@Mixin(IdMapper.class)
public abstract class IdMapperMixin<T> implements PolymerIdMapper<T> {
    @Final
    @Shadow
    @Mutable
    private List<T> idToT;
    @Shadow
    private int nextId;
    @Shadow
    @Final
    private Reference2IntMap<T> tToId;
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
        for (var value : this.idToT) {
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

        this.idToT.clear();
        this.idToT.addAll(untouched);
        this.idToT.addAll(polymer);

        this.tToId.clear();
        for (int i = 0; i < this.idToT.size(); i++) {
            this.tToId.put(this.idToT.get(i), i);
        }


        this.polymer$offset = this.idToT.size() - this.polymer$states.size();
        this.polymer$nonPolymerBitCount = Mth.ceillog2(this.idToT.size() - this.polymer$states.size());
        this.polymer$vanillaBitCount = Mth.ceillog2(this.polymer$vanillaEntryCount);
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
        this.tToId.clear();
        this.idToT.clear();
        this.polymer$vanillaEntryCount = 0;
        this.polymer$states.clear();
        this.polymer$offset = Integer.MAX_VALUE;
    }
}
