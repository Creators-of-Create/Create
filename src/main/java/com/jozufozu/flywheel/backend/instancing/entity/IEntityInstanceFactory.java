package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;

import net.minecraft.entity.Entity;

@FunctionalInterface
public interface IEntityInstanceFactory<E extends Entity> {
	EntityInstance<? super E> create(MaterialManager<?> manager, E te);
}
