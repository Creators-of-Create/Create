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

	public static KineticBlockTrigger KINETIC_BLOCK = add(new KineticBlockTrigger("kinetic_block"));

	public static SimpleTrigger 
			ROTATION = simple("rotation"), 
			OVERSTRESSED = simple("overstressed"),
			SHIFTING_GEARS = simple("shifting_gears"), 
			CONNECT_BELT = simple("connect_belt"), 
			BONK = simple("bonk"),
			WATER_WHEEL = simple("water_wheel"), 
			LAVA_WHEEL = simple("lava_wheel"), 
			DEPLOYER_BOOP = simple("deployer"),
			ABSORBED_LIGHT = simple("light_absorbed"), 
			SPEED_READ = simple("speed_read"), 
			BASIN_THROW = simple("basin"),
			PRESS_COMPACT = simple("compact"),
			UPGRADED_ZAPPER = simple("upgraded_zapper"),
			EXTENDO = simple("extendo"),
			GIGA_EXTENDO = simple("giga_extendo"),
			MIXER_MIX = simple("mixer");

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

	public static void triggerFor(ITriggerable trigger, PlayerEntity player) {
		if (player instanceof ServerPlayerEntity)
			trigger.trigger((ServerPlayerEntity) player);
	}

	public static void triggerForNearbyPlayers(ITriggerable trigger, World world, BlockPos pos, int range) {
		triggerForNearbyPlayers(trigger, world, pos, range, player -> true);
	}

	public static void triggerForNearbyPlayers(ITriggerable trigger, World world, BlockPos pos, int range,
			Predicate<PlayerEntity> playerFilter) {
		if (world == null)
			return;
		if (world.isRemote)
			return;
		List<ServerPlayerEntity> players = getPlayersInRange(world, pos, range);
		players.stream().filter(playerFilter).forEach(trigger::trigger);
	}

	public static List<ServerPlayerEntity> getPlayersInRange(World world, BlockPos pos, int range) {
		List<ServerPlayerEntity> players =
			world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(range));
		return players;
	}

}
