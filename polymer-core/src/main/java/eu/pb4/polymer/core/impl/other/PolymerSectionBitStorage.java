package eu.pb4.polymer.core.impl.other;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortIterators;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;

import java.util.BitSet;

public class PolymerSectionBitStorage {
    private ShortSet shorts = null;
    private BitSet bits = null;

    public boolean get(int index) {
        if (bits != null) {
            return bits.get(index);
        } else if (shorts != null) {
            return shorts.contains((short) index);
        }

        return false;
    }

    public void set(int index) {
        if (this.bits != null) {
            this.bits.set(index);
            return;
        }

        if (this.shorts == null) {
            this.shorts = new ShortOpenHashSet();
        }

        if (this.shorts.size() >= 32) {
            this.bits = new BitSet(16 * 16 * 16);
            for (var v : this.shorts.toShortArray()) {
                this.bits.set(v);
            }

            this.bits.set(index);
            this.shorts = null;
            return;
        }

        this.shorts.add((short) index);
    }

    public boolean remove(int index) {
        if (this.bits != null) {
            var val = this.bits.get(index);
            this.bits.clear(index);
            return val;
        } else if (this.shorts != null) {
            return this.shorts.remove((short) index);
        }

        return false;
    }

    public ShortIterator shortIterator() {
        if (this.bits != null) {
            return new ShortIterator() {
                private int index = 0;

                @Override
                public short nextShort() {
                    this.moveToNext();
                    var t = this.index;
                    this.index++;
                    return (short) t;
                }

                @Override
                public boolean hasNext() {
                    this.moveToNext();
                    return this.index < bits.length();
                }

                private void moveToNext() {
                    while (this.index < bits.length() && !bits.get(this.index)) {
                        this.index++;
                    }
                }
            };
        } else if (this.shorts != null) {
            return this.shorts.iterator();
        }

        return ShortIterators.EMPTY_ITERATOR;
    }

    public boolean isEmpty() {
        if (this.bits != null) {
            return this.bits.nextSetBit(0) == -1;
        } else if (this.shorts != null) {
            return this.shorts.isEmpty();
        }

        return true;
    }
}
