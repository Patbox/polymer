package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.PolymerIdList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;


@Mixin(IdList.class)
public abstract class IdListMixin<T> implements PolymerIdList {
    @Shadow @Mutable private List<T> list;
    @Shadow private int nextId;
    @Shadow @Final private Object2IntMap<Object> idMap;

    @Shadow public abstract void add(T value);

    @Unique private List<Object> polymer_lazyList = new ArrayList<>();
    @Unique private HashSet<BlockState> polymer_states = new HashSet<>();
    @Unique private boolean polymer_isPolymerAware;
    @Unique private boolean polymer_locked = true;
    @Unique private int polymer_offset = Integer.MAX_VALUE;
    @Unique private boolean polymer_hasPolymer = false;
    @Unique private boolean polymer_initializeLazy = true;
    @Unique private boolean polymer_reorderLock = false;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void polymer_moveToEnd(T value, CallbackInfo ci) {
        if (this.polymer_isPolymerAware && value instanceof BlockState blockState) {
            if (this.idMap.containsKey(value)) {
                ci.cancel();
                return;
            }

            if (blockState.getBlock() instanceof PolymerBlock) {
                this.polymer_states.add(blockState);

                if (this.polymer_locked) {
                    this.polymer_lazyList.add(blockState);
                    ci.cancel();
                    return;
                } else {
                    this.polymer_hasPolymer = true;
                    this.polymer_offset = Math.min(this.polymer_offset, this.nextId);
                }
            }

            if (this.polymer_hasPolymer && !(blockState.getBlock() instanceof PolymerBlock) && this.polymer_offset <= this.nextId) {
                if (this.polymer_reorderLock) {
                    PolymerImpl.LOGGER.warn("Someone registered BlockStates while StateList is locked! Related block: " + Registry.BLOCK.getId(blockState.getBlock()));
                } else {
                    if (PolymerImpl.LOG_BLOCKSTATE_REBUILDS) {
                        PolymerImpl.LOGGER.warn("Rebuilding BlockStates! Someone accessed BlockStates ids too early...");
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
                    var copy = new ArrayList<>(this.list);

                    this.polymer_clear();
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
    public Collection<BlockState> polymer_getPolymerStates() {
        return this.polymer_states;
    }

    @Inject(method = "get", at = @At("HEAD"))
    private void polymer_onGet(int index, CallbackInfoReturnable<@Nullable T> cir) {
        this.polymer_initLazy();
    }

    @Inject(method = "getRawId", at = @At("HEAD"))
    private void polymer_onGetId(T entry, CallbackInfoReturnable<Integer> cir) {
        this.polymer_initLazy();
    }

    @Inject(method = "size", at = @At("HEAD"))
    private void polymer_onSize(CallbackInfoReturnable<Integer> cir) {
        this.polymer_initLazy();
    }

    @Inject(method = "iterator", at = @At("HEAD"))
    private void polymer_onIterator(CallbackInfoReturnable<Iterator<T>> cir) {
        this.polymer_initLazy();
    }

    private void polymer_initLazy() {
        if (this.polymer_locked && this.polymer_initializeLazy) {
            if (StackWalker.getInstance().walk(PolymerImplUtils::shouldSkipStateInitialization)) {
                return;
            }

            this.polymer_offset = this.nextId;
            this.polymer_locked = false;
            ((List<T>) this.polymer_lazyList).forEach(this::add);
            this.polymer_lazyList.clear();
        }
    }

    @Override
    public void polymer_enableLazyBlockStates() {
        this.polymer_isPolymerAware = true;
    }

    @Override
    public void polymer_setIgnoreCalls(boolean value) {
        this.polymer_initializeLazy = !value;
    }

    @Override
    public int polymer_getOffset() {
        return this.polymer_offset;
    }

    @Override
    public void polymer_setReorderLock(boolean value) {
        this.polymer_reorderLock = value;
    }

    @Override
    public boolean polymer_getReorderLock() {
        return this.polymer_reorderLock;
    }

    @Override
    public void polymer_clear() {
        this.nextId = 0;
        this.idMap.clear();
        this.list.clear();
        this.polymer_lazyList.clear();
        this.polymer_states.clear();
        this.polymer_offset = Integer.MAX_VALUE;
        this.polymer_hasPolymer = false;
        this.polymer_locked = true;
    }
}
