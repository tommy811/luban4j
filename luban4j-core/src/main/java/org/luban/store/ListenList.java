package org.luban.store;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.luban.common.Assert;

import java.util.*;

/**
 * @author 鲁班大叔
 * @date 2023
 */
public class ListenList<T> implements List<T> {
    List<T> ts;
    ListChangeListener changeListener;

    public ListenList(List<T> ts, ListChangeListener changeListener) {
        Assert.notNull(ts);
        this.ts = ts;
        this.changeListener = changeListener == null ? ListChangeListener.EMPTY : changeListener;
    }


    @Override
    public int size() {

        return ts.size();
    }

    @Override
    public boolean isEmpty() {
        return ts.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return ts.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return ts.iterator();
    }

    @Override
    public Object[] toArray() {
        return ts.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return ts.toArray(a);
    }

    @Override
    public boolean add(T t) {
        boolean add = ts.add(t);
        if (add) {
            changeListener.addItem(t);
        }
        return add;
    }

    @Override
    public boolean remove(Object o) {
        boolean remove = ts.remove(o);
        if (remove) {
            changeListener.removeItem(o);
        }
        return remove;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return ts.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean b = ts.addAll(c);
        if (b) {
            c.forEach(changeListener::addItem);
        }
        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean b = ts.addAll(index, c);
        if (b) {
            c.forEach(changeListener::addItem);
        }
        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean b = ts.removeAll(c);
        if (b) {
            c.forEach(changeListener::removeItem);
        }
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return ts.retainAll(c);
    }

    @Override
    public void clear() {
        this.forEach(changeListener::removeItem);
        ts.clear();
    }

    @Override
    public T get(int index) {
        return ts.get(index);
    }

    @Override
    public T set(int index, T element) {
        T set = ts.set(index, element);
        if (set != null) {
            changeListener.removeItem(set);
        }
        changeListener.addItem(element);
        return set;
    }

    @Override
    public void add(int index, T element) {
        ts.add(index, element);
        changeListener.addItem(element);
    }

    @Override
    public T remove(int index) {
        T remove = ts.remove(index);
        if (remove != null) {
            changeListener.removeItem(remove);
        }
        return remove;
    }

    @Override
    public int indexOf(Object o) {
        return ts.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return ts.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return ts.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return ts.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return ts.subList(fromIndex, toIndex);
    }

}
