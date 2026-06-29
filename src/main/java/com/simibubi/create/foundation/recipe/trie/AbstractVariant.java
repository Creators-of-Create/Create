package com.simibubi.create.foundation.recipe.trie;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public sealed interface AbstractVariant {
	final class AbstractItem implements AbstractVariant {
		private final @NotNull Item item;
		private final int hashCode;

		public AbstractItem(@NotNull Item item) {
			this.item = item;
			this.hashCode = item.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof AbstractItem that)) return false;

			return item == that.item;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	final class AbstractFluid implements AbstractVariant {
		private final @NotNull Fluid fluid;
		private final int hashCode;

		public AbstractFluid(@NotNull Fluid fluid) {
			this.fluid = fluid;
			this.hashCode = fluid.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof AbstractFluid that)) return false;

			return fluid == that.fluid;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
