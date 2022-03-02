package com.simibubi.create.content.contraptions.processing.fan.transform;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.compress.utils.Lists;

import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public abstract class EntityTransformHelper<A extends LivingEntity, B extends LivingEntity> {

	public static final List<EntityTransformHelper<?, ?>> LIST = Lists.newArrayList();

	public static void clientEffect(AbstractFanProcessingType type, Level level, Entity entity) {
		for (EntityTransformHelper<?, ?> helper : LIST) {
			if (helper.fanType.test(type)) {
				helper._clientEffect(level, entity);
			}
		}
	}

	public static void serverEffect(AbstractFanProcessingType type, Level level, Entity entity) {
		for (EntityTransformHelper<?, ?> helper : LIST) {
			if (helper.fanType.test(type)) {
				helper._serverEffect(level, entity);
			}
		}
	}

	public final Predicate<AbstractFanProcessingType> fanType;
	public final Class<A> cls;
	public final Predicate<A> predicate;
	public final Function<A, EntityType<B>> getter;
	public final String key;

	public EntityTransformHelper(String key, Predicate<AbstractFanProcessingType> fanType, Class<A> cls, Predicate<A> predicate, Function<A, EntityType<B>> getter) {
		this.key = key;
		this.fanType = fanType;
		this.cls = cls;
		this.predicate = predicate;
		this.getter = getter;
		LIST.add(this);
	}

	private void _clientEffect(Level level, Entity entity) {
		if (cls.isInstance(entity)) {
			A a = (A) entity;
			if (predicate.test(a)) {
				clientEffect(level, a);
			}
		}
	}

	private void _serverEffect(Level level, Entity entity) {
		if (cls.isInstance(entity)) {
			A a = (A) entity;
			if (predicate.test(a)) {
				serverEffect(a, level);
			}
		}
	}

	public abstract void clientEffect(Level level, A entity);

	public void serverEffect(A entityOld, Level level) {
		int progress = entityOld.getPersistentData().getInt(key);
		if (progress < 100) {
			onProgress(level, entityOld, progress);
			entityOld.getPersistentData().putInt(key, progress + 1);
			return;
		}
		onComplete(level, entityOld);
		B entity_new = getter.apply(entityOld).create(level);
		CompoundTag serializeNBT = entityOld.saveWithoutId(new CompoundTag());
		serializeNBT.remove("UUID");
		postTransform(entityOld, entity_new);
		entity_new.deserializeNBT(serializeNBT);
		entity_new.setPos(entityOld.getPosition(0));
		level.addFreshEntity(entity_new);
		entityOld.discard();
	}

	public abstract void postTransform(A a, B b);

	public abstract void onProgress(Level level, A entity, int progress);

	public abstract void onComplete(Level level, A entity);

}
