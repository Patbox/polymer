package eu.pb4.polymer.impl.other;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * DO NOT USE IT OUTSIDE POLYMERS INTERNALS!!!
 * Or even in to be fair, just don't touch this.
 *
 * If you are seeing it, you either are just looking through polymer
 * or it created incompatibility. If it's the second thing,
 * maybe make an issue on github with link to your mod so we can
 * get through it.
 */
@ApiStatus.Internal
public final class DualList<T> implements List<T> {
    private final ArrayList<T> firstArrayList;
    private final ArrayList<T> offsetArrayList;
    private final int offset;

    public DualList(int sizeA, int sizeB, int offset) {
        this.firstArrayList = new ArrayList<>(sizeA);
        this.offsetArrayList = new ArrayList<>(sizeB);
        this.offset = offset;
    }

    public DualList(ArrayList<T> listA, ArrayList<T> listB, int offset) {
        this.firstArrayList = listA;
        this.offsetArrayList = listB;
        this.offset = offset;
    }
    @Deprecated
    @Override
    public int size() {
        return this.offsetArrayList.isEmpty() ? this.firstArrayList.size() : this.offset + this.offsetArrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.firstArrayList.isEmpty() && this.offsetArrayList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.firstArrayList.contains(o) && this.offsetArrayList.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<T> current = DualList.this.firstArrayList.iterator();
            private boolean base = true;

            @Override
            public boolean hasNext() {
                if (this.base && !this.current.hasNext()) {
                    this.current = DualList.this.offsetArrayList.iterator();
                    this.base = false;
                }

                return this.current.hasNext();
            }

            @Override
            public T next() {
                if (this.base && !this.current.hasNext()) {
                    this.current = DualList.this.offsetArrayList.iterator();
                    this.base = false;
                }

                return this.current.next();
            }
        };
    }

    @NotNull
    @Override
    public Object[] toArray() {
        var array = new Object[this.firstArrayList.size() + this.offsetArrayList.size()];
        System.arraycopy(this.firstArrayList.toArray(), 0, array, 0, this.firstArrayList.size());
        System.arraycopy(this.offsetArrayList.toArray(), 0, array, this.firstArrayList.size(), this.offsetArrayList.size());

        return array;
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        var array = this.toArray();
        return (T1[]) Arrays.copyOf(array, array.length, a.getClass());

    }

    @Override
    public boolean add(T t) {
        return this.firstArrayList.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return this.firstArrayList.remove(o) || this.offsetArrayList.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.firstArrayList.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return this.firstArrayList.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return this.getArrayForIndex(index).addAll(this.normalizeIndex(index), c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.firstArrayList.removeAll(c) || this.offsetArrayList.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.firstArrayList.retainAll(c) || this.offsetArrayList.retainAll(c);
    }

    @Override
    public void clear() {
        this.firstArrayList.clear();
        this.offsetArrayList.clear();
    }

    public ArrayList<T> getArrayForIndex(int index) {
        return index < this.offset ? this.firstArrayList : this.offsetArrayList;
    }

    public int normalizeIndex(int index) {
        return index < this.offset ? index : index - this.offset;
    }

    @Override
    public T get(int index) {
        var array = this.getArrayForIndex(index);
        index = normalizeIndex(index);

        return array.size() > index ? array.get(index) : null;
    }

    @Override
    public T set(int index, T element) {
        var array = this.getArrayForIndex(index);
        index = normalizeIndex(index);
        while(array.size() <= index) {
            array.add(null);
        }

        return array.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        var array = this.getArrayForIndex(index);
        index = normalizeIndex(index);

        array.add(index, element);
    }

    @Override
    public T remove(int index) {
        var array = this.getArrayForIndex(index);
        index = normalizeIndex(index);

        return array.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        int i = this.firstArrayList.indexOf(o);
        return i == -1 ? i : this.offsetArrayList.indexOf(o) + this.offset;
    }

    @Override
    public int lastIndexOf(Object o) {
        int i = this.firstArrayList.lastIndexOf(o);
        return i == -1 ? i : this.offsetArrayList.lastIndexOf(o) + this.offset;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        throw new RuntimeException("Sorry it's not implemented! If you need that yell at me here: https://github.com/Patbox/polymer/issues");
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        throw new RuntimeException("Sorry it's not implemented! If you need that yell at me here: https://github.com/Patbox/polymer/issues");
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new RuntimeException("Sorry it's not implemented! If you need that yell at me here: https://github.com/Patbox/polymer/issues");
    }
    public int sizeMain() {
        return this.firstArrayList.size();
    }

    public int sizeOffset() {
        return this.offsetArrayList.size();
    }

    public ArrayList<T> getMainList() {
        return this.firstArrayList;
    }

    public ArrayList<T> getOffsetList() {
        return this.offsetArrayList;
    }
}
