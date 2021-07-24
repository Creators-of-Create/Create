package com.simibubi.create;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class AllBlockPartials {

	public static final PartialModel SCHEMATICANNON_CONNECTOR = get("schematicannon/connector"),
		SCHEMATICANNON_PIPE = get("schematicannon/pipe"),

		SHAFTLESS_COGWHEEL = get("cogwheel_shaftless"), SHAFT_HALF = get("shaft_half"),

		BELT_PULLEY = get("belt_pulley"), BELT_START = get("belt/start"), BELT_MIDDLE = get("belt/middle"),
		BELT_END = get("belt/end"), BELT_START_BOTTOM = get("belt/start_bottom"),
		BELT_MIDDLE_BOTTOM = get("belt/middle_bottom"), BELT_END_BOTTOM = get("belt/end_bottom"),
		BELT_DIAGONAL_START = get("belt/diagonal_start"), BELT_DIAGONAL_MIDDLE = get("belt/diagonal_middle"),
		BELT_DIAGONAL_END = get("belt/diagonal_end"),

		ENCASED_FAN_INNER = get("encased_fan/propeller"), HAND_CRANK_HANDLE = get("hand_crank/handle"),
		MECHANICAL_PRESS_HEAD = get("mechanical_press/head"), MECHANICAL_MIXER_POLE = get("mechanical_mixer/pole"),
		MECHANICAL_MIXER_HEAD = get("mechanical_mixer/head"), MECHANICAL_CRAFTER_LID = get("mechanical_crafter/lid"),
		MECHANICAL_CRAFTER_ARROW = get("mechanical_crafter/arrow"),
		MECHANICAL_CRAFTER_BELT_FRAME = get("mechanical_crafter/belt"),
		MECHANICAL_CRAFTER_BELT = get("mechanical_crafter/belt_animated"),
		SAW_BLADE_HORIZONTAL_ACTIVE = get("mechanical_saw/blade_horizontal_active"),
		SAW_BLADE_HORIZONTAL_INACTIVE = get("mechanical_saw/blade_horizontal_inactive"),
		SAW_BLADE_HORIZONTAL_REVERSED = get("mechanical_saw/blade_horizontal_reversed"),
		SAW_BLADE_VERTICAL_ACTIVE = get("mechanical_saw/blade_vertical_active"),
		SAW_BLADE_VERTICAL_INACTIVE = get("mechanical_saw/blade_vertical_inactive"),
		SAW_BLADE_VERTICAL_REVERSED = get("mechanical_saw/blade_vertical_reversed"), GAUGE_DIAL = get("gauge/dial"),
		GAUGE_INDICATOR = get("gauge/indicator"), GAUGE_HEAD_SPEED = get("gauge/speedometer/head"),
		GAUGE_HEAD_STRESS = get("gauge/stressometer/head"), BEARING_TOP = get("bearing/top"),
		BEARING_TOP_WOODEN = get("bearing/top_wooden"), DRILL_HEAD = get("mechanical_drill/head"),
		HARVESTER_BLADE = get("mechanical_harvester/blade"), DEPLOYER_POLE = get("deployer/pole"),
		DEPLOYER_HAND_POINTING = get("deployer/hand_pointing"), DEPLOYER_HAND_PUNCHING = get("deployer/hand_punching"),
		DEPLOYER_HAND_HOLDING = get("deployer/hand_holding"), ANALOG_LEVER_HANDLE = get("analog_lever/handle"),
		ANALOG_LEVER_INDICATOR = get("analog_lever/indicator"), FUNNEL_FLAP = get("funnel/flap"),
		BELT_FUNNEL_FLAP = get("belt_funnel/flap"), BELT_TUNNEL_FLAP = get("belt_tunnel/flap"),
		FLEXPEATER_INDICATOR = get("diodes/indicator"), FLYWHEEL = get("flywheel/wheel"),
		FLYWHEEL_UPPER_ROTATING = get("flywheel/upper_rotating_connector"),

		FLYWHEEL_LOWER_ROTATING = get("flywheel/lower_rotating_connector"),
		FLYWHEEL_UPPER_SLIDING = get("flywheel/upper_sliding_connector"),
		FLYWHEEL_LOWER_SLIDING = get("flywheel/lower_sliding_connector"),
		FURNACE_GENERATOR_FRAME = get("furnace_engine/frame"), CUCKOO_MINUTE_HAND = get("cuckoo_clock/minute_hand"),
		CUCKOO_HOUR_HAND = get("cuckoo_clock/hour_hand"), CUCKOO_LEFT_DOOR = get("cuckoo_clock/left_door"),
		CUCKOO_RIGHT_DOOR = get("cuckoo_clock/right_door"), CUCKOO_PIG = get("cuckoo_clock/pig"),
		CUCKOO_CREEPER = get("cuckoo_clock/creeper"),

		GANTRY_COGS = get("gantry_carriage/wheels"),

		ROPE_COIL = get("rope_pulley/rope_coil"), ROPE_HALF = get("rope_pulley/rope_half"),
		ROPE_HALF_MAGNET = get("rope_pulley/rope_half_magnet"),

		HOSE_COIL = get("hose_pulley/rope_coil"), HOSE = get("hose_pulley/rope"),
		HOSE_MAGNET = get("hose_pulley/pulley_magnet"), HOSE_HALF = get("hose_pulley/rope_half"),
		HOSE_HALF_MAGNET = get("hose_pulley/rope_half_magnet"),

		MILLSTONE_COG = get("millstone/inner"),

		SYMMETRY_PLANE = get("symmetry_effect/plane"), SYMMETRY_CROSSPLANE = get("symmetry_effect/crossplane"),
		SYMMETRY_TRIPLEPLANE = get("symmetry_effect/tripleplane"),

		STICKER_HEAD = get("sticker/head"),

		PORTABLE_STORAGE_INTERFACE_MIDDLE = get("portable_storage_interface/block_middle"),
		PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED = get("portable_storage_interface/block_middle_powered"),
		PORTABLE_STORAGE_INTERFACE_TOP = get("portable_storage_interface/block_top"),

		PORTABLE_FLUID_INTERFACE_MIDDLE = get("portable_fluid_interface/block_middle"),
		PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED = get("portable_fluid_interface/block_middle_powered"),
		PORTABLE_FLUID_INTERFACE_TOP = get("portable_fluid_interface/block_top"),

		ARM_COG = get("mechanical_arm/cog"), ARM_BASE = get("mechanical_arm/base"),
		ARM_LOWER_BODY = get("mechanical_arm/lower_body"), ARM_UPPER_BODY = get("mechanical_arm/upper_body"),
		ARM_HEAD = get("mechanical_arm/head"), ARM_CLAW_BASE = get("mechanical_arm/claw_base"),
		ARM_CLAW_GRIP = get("mechanical_arm/claw_grip"),

		FLAG_SHORT_IN = get("mechanical_arm/flag/short_in"), FLAG_SHORT_OUT = get("mechanical_arm/flag/short_out"),
		FLAG_LONG_IN = get("mechanical_arm/flag/long_in"), FLAG_LONG_OUT = get("mechanical_arm/flag/long_out"),

		MECHANICAL_PUMP_ARROW = get("mechanical_pump/arrow"), MECHANICAL_PUMP_COG = get("mechanical_pump/cog"),
		FLUID_PIPE_CASING = get("fluid_pipe/casing"), FLUID_VALVE_POINTER = get("fluid_valve/pointer"),

		SPOUT_TOP = get("spout/top"), SPOUT_MIDDLE = get("spout/middle"), SPOUT_BOTTOM = get("spout/bottom"),

		PECULIAR_BELL = get("peculiar_bell"),
		HAUNTED_BELL = get("haunted_bell"),

	SPEED_CONTROLLER_BRACKET = get("rotation_speed_controller/bracket"),

	GOGGLES = get("goggles"),

	EJECTOR_TOP = get("weighted_ejector/top"),

	COPPER_BACKTANK_SHAFT = get("copper_backtank/block_shaft_input"),
			COPPER_BACKTANK_COGS = get("copper_backtank/block_cogs"),

	CRAFTING_BLUEPRINT_1x1 = getEntity("crafting_blueprint_small"),
			CRAFTING_BLUEPRINT_2x2 = getEntity("crafting_blueprint_medium"),
			CRAFTING_BLUEPRINT_3x3 = getEntity("crafting_blueprint_large"),

	COUPLING_ATTACHMENT = getEntity("minecart_coupling/attachment"),
			COUPLING_RING = getEntity("minecart_coupling/ring"),
			COUPLING_CONNECTOR = getEntity("minecart_coupling/connector");

	public static final Map<FluidTransportBehaviour.AttachmentTypes, Map<Direction, PartialModel>> PIPE_ATTACHMENTS = new HashMap<>();
	public static final Map<BlazeBurnerBlock.HeatLevel, PartialModel> BLAZES = new HashMap<>();

	static {
		populateMaps();
	}

	static void populateMaps() {
		for (FluidTransportBehaviour.AttachmentTypes type : FluidTransportBehaviour.AttachmentTypes.values()) {
			if (!type.hasModel())
				continue;
			Map<Direction, PartialModel> map = new HashMap<>();
			for (Direction d : Iterate.directions) {
				String asId = Lang.asId(type.name());
				map.put(d, get("fluid_pipe/" + asId + "/" + Lang.asId(d.getSerializedName())));
			}
			PIPE_ATTACHMENTS.put(type, map);
		}
		for (BlazeBurnerBlock.HeatLevel heat : BlazeBurnerBlock.HeatLevel.values()) {
			if (heat == BlazeBurnerBlock.HeatLevel.NONE)
				continue;
			BLAZES.put(heat, get("blaze_burner/blaze/" + heat.getSerializedName()));
		}
	}

	private static PartialModel getEntity(String path) {
		return new PartialModel(new ResourceLocation(Create.ID, "entity/" + path));
	}

	private static PartialModel get(String path) {
		return new PartialModel(new ResourceLocation(Create.ID, "block/" + path));
	}

	public static void clientInit() {
		// init static fields
	}
}
