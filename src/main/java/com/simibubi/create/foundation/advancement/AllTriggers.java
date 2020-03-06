package com.simibubi.create.foundation.advancement;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AllTriggers {

	private static List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

	public static SandpaperUseTrigger SANDPAPER_USE = add(new SandpaperUseTrigger("sandpaper_use"));
	public static SimpleTrigger DEPLOYER_BOOP = simple("deployer");
	public static SimpleTrigger ABSORBED_LIGHT = simple("light_absorbed");
	public static SimpleTrigger SPEED_READ = simple("speed_read");
	public static SimpleTrigger OVERSTRESSED = simple("overstressed");
	public static SimpleTrigger ROTATION = simple("rotation");

	private static SimpleTrigger simple(String id) {
		return add(new SimpleTrigger(id));
	}
	
	private static <T extends CriterionTriggerBase<?>> T add(T instance) {
		triggers.add(instance);
		return instance;
	}

	public static void register() {
		triggers.forEach(CriteriaTriggers::register);
	}

	public static void triggerForNearbyPlayers(SimpleTrigger trigger, World world, BlockPos pos, int range) {
		triggerForNearbyPlayers(trigger, world, pos, range, player -> true);
	}

	public static void triggerForNearbyPlayers(SimpleTrigger trigger, World world, BlockPos pos, int range,
			Predicate<PlayerEntity> playerFilter) {
		if (world == null)
			return;
		if (world.isRemote)
			return;
		List<ServerPlayerEntity> players =
			world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(range));
		players.stream().filter(playerFilter).forEach(trigger::trigger);
	}

}
