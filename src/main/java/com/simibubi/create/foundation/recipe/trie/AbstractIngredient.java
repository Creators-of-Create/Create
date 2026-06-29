package com.simibubi.create.foundation.recipe.trie;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class AbstractIngredient {
	final Set<AbstractVariant> variants;
	final int hashCode;

	public AbstractIngredient(Set<AbstractVariant> variants) {
		this.variants = ImmutableSet.copyOf(variants);
		this.hashCode = variants.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractIngredient that)) return false;
		if (this == that) return true;

		return this.hashCode == that.hashCode && this.variants.equals(that.variants);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	// For any ingredients that aren't representable as a finite set of variants, to handle fabric's CustomIngredient
	public static class Universal extends AbstractIngredient {
		public static final Universal INSTANCE = new Universal();
		private static final int hashCode = Universal.class.hashCode();

		private Universal() {
			super(Set.of());
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Universal;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
