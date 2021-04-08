package com.simibubi.create.foundation.utility;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.WeakHashMap;

import net.minecraft.util.Unit;

public class WeakHashSet<T> extends AbstractSet<T> {

	WeakHashMap<T, Unit> map;

	public WeakHashSet() {
		map = new WeakHashMap<>();
	}

	/**
	 * Constructs a new set containing the elements in the specified
	 * collection. The <tt>HashMap</tt> is created with default load factor
	 * (0.75) and an initial capacity sufficient to contain the elements in
	 * the specified collection.
	 *
	 * @param c the collection whose elements are to be placed into this set
	 * @throws NullPointerException if the specified collection is null
	 */
	public WeakHashSet(Collection<? extends T> c) {
		map = new WeakHashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
	 * the specified initial capacity and the specified load factor.
	 *
	 * @param initialCapacity the initial capacity of the hash map
	 * @param loadFactor      the load factor of the hash map
	 * @throws IllegalArgumentException if the initial capacity is less
	 *                                  than zero, or if the load factor is nonpositive
	 */
	public WeakHashSet(int initialCapacity, float loadFactor) {
		map = new WeakHashMap<>(initialCapacity, loadFactor);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
	 * the specified initial capacity and default load factor (0.75).
	 *
	 * @param initialCapacity the initial capacity of the hash table
	 * @throws IllegalArgumentException if the initial capacity is less
	 *                                  than zero
	 */
	public WeakHashSet(int initialCapacity) {
		map = new WeakHashMap<>(initialCapacity);
	}

	@Override
	public Iterator<T> iterator() {
		return map.keySet()
			.iterator();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean add(T t) {
		return map.put(t, Unit.INSTANCE) == null;
	}

	@Override
	public boolean remove(Object o) {
		return map.remove((T) o) != null;
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsKey((T) o);
	}

	@Override
	public Object[] toArray() {
		return map.keySet()
			.toArray();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return c.stream()
			.allMatch(map::containsKey);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {
		map.clear();
	}
}
