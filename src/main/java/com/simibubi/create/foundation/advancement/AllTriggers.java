package com.simibubi.create.foundation.advancement;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.content.contraptions.processing.InWorldProcessing;

import net.fabricmc.fabric.mixin.object.builder.CriteriaAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

public class AllTriggers {

	private static final List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

	public static final StringSerializableTrigger<Fluid> INFINITE_FLUID =
		add(new RegistryTrigger<>("infinite_fluid", Registry.FLUID));
	public static final StringSerializableTrigger<Block> BRACKET_APPLY_TRIGGER =
		add(new RegistryTrigger<>("bracket_apply", Registry.BLOCK));
	public static final StringSerializableTrigger<InWorldProcessing.Type> FAN_PROCESSING =
		add(new EnumTrigger<>("fan_processing", InWorldProcessing.Type.class));

	public static final SimpleTrigger ROTATION = simple("rotation"), OVERSTRESSED = simple("overstressed"),
		SHIFTING_GEARS = simple("shifting_gears"), CONNECT_BELT = simple("connect_belt"), BONK = simple("bonk"),
		WATER_WHEEL = simple("water_wheel"), LAVA_WHEEL = simple("lava_wheel"),
		CHOCOLATE_WHEEL = simple("chocolate_wheel"), DEPLOYER_BOOP = simple("deployer"),
		SPEED_READ = simple("speed_read"), BASIN_THROW = simple("basin"), PRESS_COMPACT = simple("compact"),
		UPGRADED_ZAPPER = simple("upgraded_zapper"), EXTENDO = simple("extendo"), GIGA_EXTENDO = simple("giga_extendo"),
		MECHANICAL_ARM = simple("mechanical_arm"), MUSICAL_ARM = simple("musical_arm"), CUCKOO = simple("cuckoo"),
		CASING_SHAFT = simple("casing_shaft"), CASING_BELT = simple("casing_belt"), CASING_PIPE = simple("casing_pipe"),
		WINDMILL = simple("windmill"), MAXED_WINDMILL = simple("maxed_windmill"), PLACE_TUNNEL = simple("place_tunnel"),
		CONNECT_TUNNEL = simple("connect_tunnel"), UPWARD_CHUTE = simple("upward_chute"),
		BELT_FUNNEL = simple("belt_funnel"), BELT_FUNNEL_KISS = simple("belt_funnel_kiss"),
		CLOCKWORK_BEARING = simple("clockwork_bearing"), ARM_MANY_TARGETS = simple("arm_many_targets"),
		ARM_BLAZE_BURNER = simple("arm_blaze_burner"), FLYWHEEL = simple("flywheel"),
		OVERSTRESS_FLYWHEEL = simple("overstress_flywheel"), ITEM_DRAIN = simple("item_drain"),
		CHAINED_ITEM_DRAIN = simple("chained_item_drain"), SPOUT = simple("spout"),
		SPOUT_POTION = simple("spout_potion"), GLASS_PIPE = simple("glass_pipe"),
		PIPE_COLLISION = simple("pipe_collision"), PIPE_SPILL = simple("pipe_spill"),
		POTATO_KILL = simple("potato_kill"), HOSE_PULLEY = simple("hose_pulley"), MIXER_MIX = simple("mixer");

	private static SimpleTrigger simple(String id) {
		return add(new SimpleTrigger(id));
	}

	private static <T extends CriterionTriggerBase<?>> T add(T instance) {
		triggers.add(instance);
		return instance;
	}

	public static void register() {
		triggers.forEach(CriteriaAccessor::callRegister);
	}

	public static void triggerFor(ITriggerable trigger, Player player) {
		if (player instanceof ServerPlayer)
			trigger.trigger((ServerPlayer) player);
	}

	public static void triggerForNearbyPlayers(ITriggerable trigger, LevelAccessor world, BlockPos pos, int range) {
		triggerForNearbyPlayers(trigger, world, pos, range, player -> true);
	}

	public static void triggerForNearbyPlayers(ITriggerable trigger, LevelAccessor world, BlockPos pos, int range,
		Predicate<Player> playerFilter) {
		if (world == null)
			return;
		if (world.isClientSide())
			return;
		List<ServerPlayer> players = getPlayersInRange(world, pos, range);
		players.stream()
			.filter(playerFilter)
			.forEach(trigger::trigger);
	}

	public static List<ServerPlayer> getPlayersInRange(LevelAccessor world, BlockPos pos, int range) {
		return world.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(range));
	}
}
