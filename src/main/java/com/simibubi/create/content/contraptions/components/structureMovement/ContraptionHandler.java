package com.simibubi.create.content.contraptions.components.structureMovement;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ContraptionHandler {

	public static Cache<World, List<WeakReference<ContraptionEntity>>> activeContraptions = CacheBuilder.newBuilder()
		.expireAfterAccess(400, SECONDS)
		.build();

	public static void addSpawnedContraptionsToCollisionList(Entity entity, World world) {
		if (!(entity instanceof ContraptionEntity))
			return;
		try {
			List<WeakReference<ContraptionEntity>> list =
				activeContraptions.get(world, () -> Collections.synchronizedList(new ArrayList<>()));
			ContraptionEntity contraption = (ContraptionEntity) entity;
			list.add(new WeakReference<>(contraption));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static void entitiesWhoJustDismountedGetSentToTheRightLocation(LivingEntity entityLiving, World world) {
		if (world.isRemote)
			return;
		CompoundNBT data = entityLiving.getPersistentData();
		if (!data.contains("ContraptionDismountLocation"))
			return;
		Vec3d position = VecHelper.readNBT(data.getList("ContraptionDismountLocation", NBT.TAG_DOUBLE));
		if (entityLiving.getRidingEntity() == null)
			entityLiving.setPositionAndUpdate(position.x, position.y, position.z);
		data.remove("ContraptionDismountLocation");
	}

}
