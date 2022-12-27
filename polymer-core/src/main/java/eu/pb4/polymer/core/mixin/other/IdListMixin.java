package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.collection.IdList;
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
    private Object2IntMap<Object> idMap;

    @Shadow
    public abstract void add(T value);

    @Unique
    private final List<T> polymer$lazyList = new ArrayList<>();
    @Unique
    private final HashSet<T> polymer$states = new HashSet<>();
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
    private Predicate<T> polymer$checker;
    @Unique
    private boolean polymer$isPolymerAware;
    @Unique
    private Function<T, String> polymer$namer;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void polymer$moveToEnd(T value, CallbackInfo ci) {
        if (this.polymer$isPolymerAware) {
            if (this.idMap.containsKey(value)) {
                ci.cancel();
                return;
            }

            var isPolymerObj = this.polymer$checker.test(value);

            if (isPolymerObj) {
                this.polymer$states.add(value);

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
                    PolymerImpl.LOGGER.warn("Someone registered object while IdList is locked! Related: " + this.polymer$namer.apply(value));
                } else {
                    if (PolymerImpl.LOG_BLOCKSTATE_REBUILDS) {
                        var trace = Thread.currentThread().getStackTrace();
                        if (PolymerImplUtils.shouldLogStateRebuild(trace)) {
                            PolymerImpl.LOGGER.warn("Rebuilding IdList! Someone accessed it too early...");
                            var builder = new StringBuilder();
                            var line = 0;
                            for (var stackTrace : Thread.currentThread().getStackTrace()) {
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

    private void polymer$initLazy() {
        if (this.polymer$locked && this.polymer$initializeLazy) {
            if (StackWalker.getInstance().walk(PolymerImplUtils::shouldSkipStateInitialization)) {
                return;
            }

            this.polymer$offset = this.nextId;
            this.polymer$locked = false;
            this.polymer$lazyList.forEach(this::add);
            this.polymer$lazyList.clear();
        }
    }

    @Override
    public void polymer$setChecker(Predicate<T> function, Function<T, String> namer) {
        this.polymer$checker = function;
        this.polymer$isPolymerAware = function != null;
        this.polymer$namer = namer;
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
        this.polymer$lazyList.clear();
        this.polymer$states.clear();
        this.polymer$offset = Integer.MAX_VALUE;
        this.polymer$hasPolymer = false;
        this.polymer$locked = true;
    }
}
