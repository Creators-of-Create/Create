package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;

public class SchematicWorldEntityGetter implements LevelEntityGetter<Entity> {

	public List<Entity> entities;

	public SchematicWorldEntityGetter() {
		this.entities = new ArrayList<>();
	}

	@Nullable
	@Override
	public Entity get(int p_156931_) {
		return null;
	}

	@Nullable
	@Override
	public Entity get(UUID pUuid) {
		for (Entity entity : entities) {
			if(entity.getUUID() == pUuid) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public Iterable<Entity> getAll() {
		return entities;
	}

	@Override
	public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156935_, Consumer<U> p_156936_) {
	}

	@Override
	public void get(AABB p_156937_, Consumer<Entity> p_156938_) {
	}

	@Override
	public <U extends Entity> void get(EntityTypeTest<Entity, U> p_156932_, AABB p_156933_, Consumer<U> p_156934_) {
	}
}
