package com.simibubi.create;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.redstone.displayLink.source.AccumulatedItemCountDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.BoilerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ComputerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.CurrentFloorDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.DeathCounterDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.EnchantPowerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.EntityNameDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FactoryGaugeDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FillLevelDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FluidAmountDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.FluidListDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemCountDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemListDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemNameDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ItemThroughputDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.KineticSpeedDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.KineticStressDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.NixieTubeDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ObservedTrainNameSource;
import com.simibubi.create.content.redstone.displayLink.source.PackageAddressDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.RedstonePowerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ScoreboardDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.StationSummaryDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.StopWatchDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.TimeOfDayDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.TrainStatusDisplaySource;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AllDisplaySources {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final RegistryEntry<DisplaySource, DeathCounterDisplaySource> DEATH_COUNT = REGISTRATE.displaySource("death_count", DeathCounterDisplaySource::new)
		.associate(Blocks.RESPAWN_ANCHOR)
		.register();
	public static final RegistryEntry<DisplaySource, ScoreboardDisplaySource> SCOREBOARD = REGISTRATE.displaySource("scoreboard", ScoreboardDisplaySource::new)
		.associate(BlockEntityType.COMMAND_BLOCK)
		.register();
	public static final RegistryEntry<DisplaySource, EnchantPowerDisplaySource> ENCHANT_POWER = REGISTRATE.displaySource("enchant_power", EnchantPowerDisplaySource::new)
		.associate(BlockEntityType.ENCHANTING_TABLE)
		.register();
	public static final RegistryEntry<DisplaySource, RedstonePowerDisplaySource> REDSTONE_POWER = REGISTRATE.displaySource("redstone_power", RedstonePowerDisplaySource::new)
		.associate(Blocks.TARGET)
		.register();

	public static final RegistryEntry<DisplaySource, NixieTubeDisplaySource> NIXIE_TUBE = simple("nixie_tube", NixieTubeDisplaySource::new);
	public static final RegistryEntry<DisplaySource, ItemNameDisplaySource> ITEM_NAMES = simple("item_names", ItemNameDisplaySource::new);
	public static final RegistryEntry<DisplaySource, BoilerDisplaySource> BOILER = simple("boiler", BoilerDisplaySource::new);
	public static final RegistryEntry<DisplaySource, CurrentFloorDisplaySource> CURRENT_FLOOR = simple("current_floor", CurrentFloorDisplaySource::new);
	public static final RegistryEntry<DisplaySource, FillLevelDisplaySource> FILL_LEVEL = simple("fill_level", FillLevelDisplaySource::new);
	public static final RegistryEntry<DisplaySource, FactoryGaugeDisplaySource> GAUGE_STATUS = simple("gauge_status", FactoryGaugeDisplaySource::new);
	public static final RegistryEntry<DisplaySource, EntityNameDisplaySource> ENTITY_NAME = simple("entity_name", EntityNameDisplaySource::new);

	public static final RegistryEntry<DisplaySource, TimeOfDayDisplaySource> TIME_OF_DAY = simple("time_of_day", TimeOfDayDisplaySource::new);
	public static final RegistryEntry<DisplaySource, StopWatchDisplaySource> STOPWATCH = simple("stopwatch", StopWatchDisplaySource::new);

	public static final RegistryEntry<DisplaySource, KineticSpeedDisplaySource> KINETIC_SPEED = simple("kinetic_speed", KineticSpeedDisplaySource::new);
	public static final RegistryEntry<DisplaySource, KineticStressDisplaySource> KINETIC_STRESS = simple("kinetic_stress", KineticStressDisplaySource::new);

	public static final RegistryEntry<DisplaySource, StationSummaryDisplaySource> STATION_SUMMARY = simple("station_summary", StationSummaryDisplaySource::new);
	public static final RegistryEntry<DisplaySource, TrainStatusDisplaySource> TRAIN_STATUS = simple("train_status", TrainStatusDisplaySource::new);
	public static final RegistryEntry<DisplaySource, ObservedTrainNameSource> OBSERVED_TRAIN_NAME = simple("observed_train_name", ObservedTrainNameSource::new);

	public static final RegistryEntry<DisplaySource, AccumulatedItemCountDisplaySource> ACCUMULATE_ITEMS = simple("accumulate_items", AccumulatedItemCountDisplaySource::new);
	public static final RegistryEntry<DisplaySource, ItemThroughputDisplaySource> ITEM_THROUGHPUT = simple("item_throughput", ItemThroughputDisplaySource::new);

	public static final RegistryEntry<DisplaySource, ItemCountDisplaySource> COUNT_ITEMS = simple("count_items", ItemCountDisplaySource::new);
	public static final RegistryEntry<DisplaySource, ItemListDisplaySource> LIST_ITEMS = simple("list_items", ItemListDisplaySource::new);
	public static final RegistryEntry<DisplaySource, FluidAmountDisplaySource> COUNT_FLUIDS = simple("count_fluids", FluidAmountDisplaySource::new);
	public static final RegistryEntry<DisplaySource, FluidListDisplaySource> LIST_FLUIDS = simple("list_fluids", FluidListDisplaySource::new);
	public static final RegistryEntry<DisplaySource, PackageAddressDisplaySource> READ_PACKAGE_ADDRESS = simple("read_package_address", PackageAddressDisplaySource::new);

	public static final RegistryEntry<DisplaySource, ComputerDisplaySource> COMPUTER = REGISTRATE.displaySource("computer", ComputerDisplaySource::new)
		.onRegisterAfter(Registries.BLOCK_ENTITY_TYPE, source -> {
			if (!Mods.COMPUTERCRAFT.isLoaded())
				return;

			List<String> types = List.of("wired_modem_full", "computer_normal", "computer_advanced", "computer_command");
			for (String name : types) {
				ResourceLocation id = Mods.COMPUTERCRAFT.rl(name);
				if (BuiltInRegistries.BLOCK_ENTITY_TYPE.containsKey(id)) {
					BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(id);
					DisplaySource.BY_BLOCK_ENTITY.add(type, source);
				} else {
					Create.LOGGER.warn("Could not find block entity type {}. Outdated compat?", id);
				}
			}
		})
		.register();

	public static final Map<String, RegistryEntry<DisplaySource, ? extends DisplaySource>> LEGACY_NAMES = Util.make(() -> {
		Map<String, RegistryEntry<DisplaySource, ? extends DisplaySource>> map = new HashMap<>();
		map.put("death_count_display_source", DEATH_COUNT);
		map.put("scoreboard_display_source", SCOREBOARD);
		map.put("enchant_power_display_source", ENCHANT_POWER);
		map.put("redstone_power_display_source", REDSTONE_POWER);

		map.put("nixie_tube_source", NIXIE_TUBE);
		map.put("belt_source_combine_item_names", ITEM_NAMES);
		map.put("cuckoo_clock_source_time_of_day", TIME_OF_DAY);
		map.put("cuckoo_clock_source_stop_watch", STOPWATCH);
		map.put("speedometer_source_kinetic_speed", KINETIC_SPEED);
		map.put("stressometer_source_kinetic_stress", KINETIC_STRESS);
		map.put("fluid_tank_source_boiler_status", BOILER);
		map.put("elevator_contact_source_current_floor", CURRENT_FLOOR);
		map.put("track_station_source_station_summary", STATION_SUMMARY);
		map.put("track_station_source_train_status", TRAIN_STATUS);
		map.put("track_observer_source_observed_train_name", OBSERVED_TRAIN_NAME);

		map.put("andesite_tunnel_source_accumulate_items", ACCUMULATE_ITEMS);
		map.put("andesite_tunnel_source_item_throughput", ITEM_THROUGHPUT);
		map.put("brass_tunnel_source_accumulate_items", ACCUMULATE_ITEMS);
		map.put("brass_tunnel_source_item_throughput", ITEM_THROUGHPUT);

		map.put("content_observer_source_count_items", COUNT_ITEMS);
		map.put("content_observer_source_list_items", LIST_ITEMS);
		map.put("content_observer_source_count_fluids", COUNT_FLUIDS);
		map.put("content_observer_source_list_fluids", LIST_FLUIDS);

		map.put("stockpile_switch_source_fill_level", FILL_LEVEL);

		map.put("factory_gauge_source_gauge_status", GAUGE_STATUS);

		for (DyeColor color : DyeColor.values()) {
			String name = color.getSerializedName() + "_seat_source_entity_name";
			map.put(name, ENTITY_NAME);
		}

		map.put("depot_source_combine_item_names", ITEM_NAMES);
		map.put("weighted_ejector_source_combine_item_names", ITEM_NAMES);

		map.put("computer_display_source", COMPUTER);

		return map;
	});

	private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(String name, Supplier<T> supplier) {
		return REGISTRATE.displaySource(name, supplier).register();
	}

	public static void register() {
	}
}
