package com.simibubi.create.content.contraptions.processing.fan.transform;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;

public class HorseTransform extends HauntingEntityTransform<Horse, SkeletonHorse> {

	public HorseTransform() {
		super(Horse.class, e -> true, a -> EntityType.SKELETON_HORSE);
	}

	@Override
	public void postTransform(Horse horse, SkeletonHorse skeletonHorse) {
		if (!horse.getArmor().isEmpty())
			horse.spawnAtLocation(horse.getArmor());
	}

}
