package com.simibubi.create.foundation.advancement;

import com.simibubi.create.content.logistics.InWorldProcessing;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class AllTriggers {

	private static final List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

	public static final StringSerializableTrigger<Fluid> INFINITE_FLUID = add(new RegistryTrigger<>("infinite_fluid", ForgeRegistries.FLUIDS));
	public static final StringSerializableTrigger<Block> BRACKET_APPLY_TRIGGER = add(new RegistryTrigger<>("bracket_apply", ForgeRegistries.BLOCKS));
	public static final StringSerializableTrigger<InWorldProcessing.Type> FAN_PROCESSING = add(new EnumTrigger<>("fan_processing", InWorldProcessing.Type.class));

	public static final SimpleTrigger
			ROTATION = simple("rotation"), 
			OVERSTRESSED = simple("overstressed"),
			SHIFTING_GEARS = simple("shifting_gears"), 
			CONNECT_BELT = simple("connect_belt"), 
			BONK = simple("bonk"),
			WATER_WHEEL = simple("water_wheel"), 
			LAVA_WHEEL = simple("lava_wheel"), 
			CHOCOLATE_WHEEL = simple("chocolate_wheel"), 
			DEPLOYER_BOOP = simple("deployer"),
			SPEED_READ = simple("speed_read"), 
			BASIN_THROW = simple("basin"),
			PRESS_COMPACT = simple("compact"),
			UPGRADED_ZAPPER = simple("upgraded_zapper"),
			EXTENDO = simple("extendo"),
			GIGA_EXTENDO = simple("giga_extendo"),
			MECHANICAL_ARM = simple("mechanical_arm"),
			MUSICAL_ARM = simple("musical_arm"),
			CUCKOO = simple("cuckoo"),
			CASING_SHAFT = simple("casing_shaft"),
			CASING_BELT = simple("casing_belt"),
			CASING_PIPE = simple("casing_pipe"),
			WINDMILL = simple("windmill"),
			MAXED_WINDMILL = simple("maxed_windmill"),
			PLACE_TUNNEL = simple("place_tunnel"),
			CONNECT_TUNNEL = simple("connect_tunnel"),
			UPWARD_CHUTE = simple("upward_chute"),
			BELT_FUNNEL = simple("belt_funnel"),
			BELT_FUNNEL_KISS = simple("belt_funnel_kiss"),
			CLOCKWORK_BEARING = simple("clockwork_bearing"),
			ARM_MANY_TARGETS = simple("arm_many_targets"),
			ARM_BLAZE_BURNER = simple("arm_blaze_burner"),
			FLYWHEEL = simple("flywheel"),
			OVERSTRESS_FLYWHEEL = simple("overstress_flywheel"),
			ITEM_DRAIN = simple("item_drain"),
			CHAINED_ITEM_DRAIN = simple("chained_item_drain"),
			SPOUT = simple("spout"),
			SPOUT_POTION = simple("spout_potion"),
			GLASS_PIPE = simple("glass_pipe"),
			PIPE_COLLISION = simple("pipe_collision"),
			PIPE_SPILL = simple("pipe_spill"),
			HOSE_PULLEY = simple("hose_pulley"),
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

	public static void triggerForNearbyPlayers(ITriggerable trigger, IWorld world, BlockPos pos, int range) {
		triggerForNearbyPlayers(trigger, world, pos, range, player -> true);
	}

	public static void triggerForNearbyPlayers(ITriggerable trigger, IWorld world, BlockPos pos, int range,
			Predicate<PlayerEntity> playerFilter) {
		if (world == null)
			return;
		if (world.isRemote())
			return;
		List<ServerPlayerEntity> players = getPlayersInRange(world, pos, range);
		players.stream().filter(playerFilter).forEach(trigger::trigger);
	}

	public static List<ServerPlayerEntity> getPlayersInRange(IWorld world, BlockPos pos, int range) {
		return world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(range));
	}
}
