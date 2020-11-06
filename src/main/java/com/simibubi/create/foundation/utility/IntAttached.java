package com.simibubi.create.foundation.utility;

public class IntAttached<V> extends Pair<Integer, V> {

	protected IntAttached(Integer first, V second) {
		super(first, second);
	}
	
	public static <V> IntAttached<V> with(int number, V value) {
		return new IntAttached<>(number, value);
	}
	
	public static <V> IntAttached<V> withZero(V value) {
		return new IntAttached<>(0, value);
	}
	
	public boolean isZero() {
		return first.intValue() == 0;
	}
	
	public boolean isOrBelowZero() {
		return first.intValue() <= 0;
	}
	
	public void increment() {
		first++;
	}
	
	public void decrement() {
		first--;
	}
	
	public V getValue() {
		return getSecond();
	}

}
