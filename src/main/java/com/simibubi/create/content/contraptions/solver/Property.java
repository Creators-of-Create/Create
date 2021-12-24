package com.simibubi.create.content.contraptions.solver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.Objects;

public record Property<T>(BlockPos pos, Type<T> type) {
	public static class Type<T> { }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Property<?> property = (Property<?>) o;
		return Objects.equals(pos, property.pos) && Objects.equals(type, property.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, type);
	}

	public static record Relative<T>(Vec3i offset, Type<T> type) {
		public Relative(Type<T> type) {
			this(Vec3i.ZERO, type);
		}

		public Property<T> toAbsolute(BlockPos pos) {
			return new Property<>(pos.offset(offset), type);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Relative<?> relative = (Relative<?>) o;
			return Objects.equals(offset, relative.offset) && Objects.equals(type, relative.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(offset, type);
		}
	}
}
