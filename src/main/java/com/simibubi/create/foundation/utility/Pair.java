package com.simibubi.create.foundation.utility;

import java.util.Objects;

public class Pair<F, S> {

	F first;
	S second;

	protected Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public static <F, S> Pair<F, S> of(F first, S second) {
		return new Pair<>(first, second);
	}

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}
	
	public void setFirst(F first) {
		this.first = first;
	}
	
	public void setSecond(S second) {
		this.second = second;
	}
	
	public Pair<F, S> copy() {
		return Pair.of(first, second);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Pair) {
			final Pair<?, ?> other = (Pair<?, ?>) obj;
			return Objects.equals(first, other.first) && Objects.equals(second, other.second);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
	
	public Pair<S, F> swap() {
		return Pair.of(second, first);
	}

}
