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
    private final List<T> polymer$lazyList = new ArrayList<>();
    @Unique
    private final Set<T> polymer$states = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);
    @Unique
    private boolean polymer$locked = true;
    @Unique
    private int polymer$offset = Integer.MAX_VALUE;
    @Unique
    private boolean polymer$hasPolymer = false;
    @Unique
    private boolean polymer$initializeLazy = true;
    @Unique
    private boolean polymer$reorderLock = false;
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
        if (this.polymer$isPolymerAware) {
            if (this.idMap.containsKey(value)) {
                ci.cancel();
                return;
            }

            var isPolymerObj = this.polymer$polymerEntryChecker.test(value);

            if (isPolymerObj || this.polymer$serverEntryChecker.test(value)) {
                this.polymer$states.add(value);
            } else {
                this.polymer$vanillaEntryCount++;
            }

            if (isPolymerObj) {
                if (this.polymer$locked) {
                    this.polymer$lazyList.add(value);
                    ci.cancel();
                    return;
                } else {
                    this.polymer$hasPolymer = true;
                    this.polymer$offset = Math.min(this.polymer$offset, this.nextId);
                }
            }

            if (this.polymer$hasPolymer && !isPolymerObj && this.polymer$offset <= this.nextId) {
                if (this.polymer$reorderLock) {
                    PolymerImpl.LOGGER.warn("Someone registered object while IdList is locked! Related: " + this.polymer$nameCreator.apply(value));
                } else {
                    if (PolymerImpl.LOG_BLOCKSTATE_REBUILDS) {
                        var trace = Thread.currentThread().getStackTrace();
                        if (PolymerImplUtils.shouldLogStateRebuild(trace)) {
                            PolymerImpl.LOGGER.warn("Rebuilding IdList! Someone accessed it too early...");
                            var builder = new StringBuilder();
                            var line = 0;
                            for (var stackTrace : trace) {
                                if (line > 0) {
                                    builder.append("\t").append(stackTrace.toString()).append("\n");
                                }
                                if (line > 24) {
                                    break;
                                }
                                line++;
                            }
                            PolymerImpl.LOGGER.warn("Called by:\n" + builder);
                        }
                    }
                    var copy = new ArrayList<>(this.list);

                    this.polymer$clear();
                    for (var entry : copy) {
                        if (entry != null) {
                            this.add(entry);
                        }
                    }
                }
            }
            this.polymer$nonPolymerBitCount = MathHelper.ceilLog2(this.list.size() - this.polymer$states.size());
            this.polymer$vanillaBitCount = MathHelper.ceilLog2(this.polymer$vanillaEntryCount);
        }
    }

    @Override
    public Collection<T> polymer$getPolymerEntries() {
        return this.polymer$states;
    }

    @Inject(method = "get", at = @At("HEAD"))
    private void polymer$onGet(int index, CallbackInfoReturnable<@Nullable T> cir) {
        this.polymer$initLazy();
    }

    @Inject(method = "getRawId", at = @At("HEAD"))
    private void polymer$onGetId(T entry, CallbackInfoReturnable<Integer> cir) {
        this.polymer$initLazy();
    }

    @Inject(method = "size", at = @At("HEAD"))
    private void polymer$onSize(CallbackInfoReturnable<Integer> cir) {
        this.polymer$initLazy();
    }

    @Inject(method = "iterator", at = @At("HEAD"))
    private void polymer$onIterator(CallbackInfoReturnable<Iterator<T>> cir) {
        this.polymer$initLazy();
    }

    @Unique
    private void polymer$initLazy() {
        if (this.polymer$locked && this.polymer$initializeLazy) {
            if (StackWalker.getInstance().walk(PolymerImplUtils::shouldSkipStateInitialization)) {
                return;
            }

            this.polymer$offset = this.nextId;
            this.polymer$locked = false;
            this.polymer$lazyList.forEach(this::add);
            this.polymer$lazyList.clear();
            this.polymer$nonPolymerBitCount = MathHelper.ceilLog2(this.list.size() - this.polymer$states.size());
            this.polymer$vanillaBitCount = MathHelper.ceilLog2(this.polymer$vanillaEntryCount);
        }
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
    public void polymer$setIgnoreCalls(boolean value) {
        this.polymer$initializeLazy = !value;
    }

    @Override
    public int polymer$getOffset() {
        return this.polymer$offset;
    }

    @Override
    public void polymer$setReorderLock(boolean value) {
        this.polymer$reorderLock = value;
    }

    @Override
    public boolean polymer$getReorderLock() {
        return this.polymer$reorderLock;
    }

    @Override
    public void polymer$clear() {
        this.nextId = 0;
        this.idMap.clear();
        this.list.clear();
        this.polymer$vanillaEntryCount = 0;
        this.polymer$lazyList.clear();
        this.polymer$states.clear();
        this.polymer$offset = Integer.MAX_VALUE;
        this.polymer$hasPolymer = false;
        this.polymer$locked = true;
    }
}
