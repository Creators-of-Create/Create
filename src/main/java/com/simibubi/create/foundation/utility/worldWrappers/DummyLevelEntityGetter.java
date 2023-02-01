package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;

public class DummyLevelEntityGetter<T extends EntityAccess> implements LevelEntityGetter<T> {

	@Override
	public T get(int p_156931_) {
		return null;
	}

	@Override
	public T get(UUID pUuid) {
		return null;
	}

	@Override
	public Iterable<T> getAll() {
		return Collections.emptyList();
	}

	@Override
	public <U extends T> void get(EntityTypeTest<T, U> p_156935_, AbortableIterationConsumer<U> p_156936_) {
	}

	@Override
	public void get(AABB p_156937_, Consumer<T> p_156938_) {
	}

	@Override
	public <U extends T> void get(EntityTypeTest<T, U> p_156932_, AABB p_156933_, AbortableIterationConsumer<U> p_156934_) {
	}

}
