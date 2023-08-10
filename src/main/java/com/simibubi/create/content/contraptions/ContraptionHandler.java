package com.simibubi.create.content.contraptions;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.WorldAttached;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ContraptionHandler {

	/* Global map of loaded contraptions */

	public static WorldAttached<Map<Integer, WeakReference<AbstractContraptionEntity>>> loadedContraptions;
	static WorldAttached<List<AbstractContraptionEntity>> queuedAdditions;

	static {
		loadedContraptions = new WorldAttached<>($ -> new HashMap<>());
		queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
	}

	public static void tick(Level world) {
		Map<Integer, WeakReference<AbstractContraptionEntity>> map = loadedContraptions.get(world);
		List<AbstractContraptionEntity> queued = queuedAdditions.get(world);

		for (AbstractContraptionEntity contraptionEntity : queued)
			map.put(contraptionEntity.getId(), new WeakReference<>(contraptionEntity));
		queued.clear();

		Collection<WeakReference<AbstractContraptionEntity>> values = map.values();
		for (Iterator<WeakReference<AbstractContraptionEntity>> iterator = values.iterator(); iterator.hasNext();) {
			WeakReference<AbstractContraptionEntity> weakReference = iterator.next();
			AbstractContraptionEntity contraptionEntity = weakReference.get();
			if (contraptionEntity == null || !contraptionEntity.isAliveOrStale()) {
				iterator.remove();
				continue;
			}
			if (!contraptionEntity.isAlive()) {
				contraptionEntity.staleTicks--;
				continue;
			}

			ContraptionCollider.collideEntities(contraptionEntity);
		}
	}

	public static void addSpawnedContraptionsToCollisionList(Entity entity, Level world) {
		if (entity instanceof AbstractContraptionEntity)
			queuedAdditions.get(world)
				.add((AbstractContraptionEntity) entity);
	}

	public static void entitiesWhoJustDismountedGetSentToTheRightLocation(LivingEntity entityLiving, Level world) {
		if (!world.isClientSide)
			return;

		CompoundTag data = entityLiving.getPersistentData();
		if (!data.contains("ContraptionDismountLocation"))
			return;

		Vec3 position = VecHelper.readNBT(data.getList("ContraptionDismountLocation", Tag.TAG_DOUBLE));
		if (entityLiving.getVehicle() == null)
			entityLiving.absMoveTo(position.x, position.y, position.z, entityLiving.getYRot(), entityLiving.getXRot());
		data.remove("ContraptionDismountLocation");
		entityLiving.setOnGround(false);
	}

}
