package com.simibubi.create;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class AllBlockPartials {

	public static final PartialModel

	SCHEMATICANNON_CONNECTOR = block("schematicannon/connector"), SCHEMATICANNON_PIPE = block("schematicannon/pipe"),

		SHAFTLESS_COGWHEEL = block("cogwheel_shaftless"), SHAFTLESS_LARGE_COGWHEEL = block("large_cogwheel_shaftless"),
		COGWHEEL_SHAFT = block("cogwheel_shaft"), SHAFT_HALF = block("shaft_half"),

		BELT_PULLEY = block("belt_pulley"), BELT_START = block("belt/start"), BELT_MIDDLE = block("belt/middle"),
		BELT_END = block("belt/end"), BELT_START_BOTTOM = block("belt/start_bottom"),
		BELT_MIDDLE_BOTTOM = block("belt/middle_bottom"), BELT_END_BOTTOM = block("belt/end_bottom"),
		BELT_DIAGONAL_START = block("belt/diagonal_start"), BELT_DIAGONAL_MIDDLE = block("belt/diagonal_middle"),
		BELT_DIAGONAL_END = block("belt/diagonal_end"),

		ENCASED_FAN_INNER = block("encased_fan/propeller"), HAND_CRANK_HANDLE = block("hand_crank/handle"),
		MECHANICAL_PRESS_HEAD = block("mechanical_press/head"), MECHANICAL_MIXER_POLE = block("mechanical_mixer/pole"),
		MECHANICAL_MIXER_HEAD = block("mechanical_mixer/head"),
		MECHANICAL_CRAFTER_LID = block("mechanical_crafter/lid"),
		MECHANICAL_CRAFTER_ARROW = block("mechanical_crafter/arrow"),
		MECHANICAL_CRAFTER_BELT_FRAME = block("mechanical_crafter/belt"),
		MECHANICAL_CRAFTER_BELT = block("mechanical_crafter/belt_animated"),
		SAW_BLADE_HORIZONTAL_ACTIVE = block("mechanical_saw/blade_horizontal_active"),
		SAW_BLADE_HORIZONTAL_INACTIVE = block("mechanical_saw/blade_horizontal_inactive"),
		SAW_BLADE_HORIZONTAL_REVERSED = block("mechanical_saw/blade_horizontal_reversed"),
		SAW_BLADE_VERTICAL_ACTIVE = block("mechanical_saw/blade_vertical_active"),
		SAW_BLADE_VERTICAL_INACTIVE = block("mechanical_saw/blade_vertical_inactive"),
		SAW_BLADE_VERTICAL_REVERSED = block("mechanical_saw/blade_vertical_reversed"), GAUGE_DIAL = block("gauge/dial"),
		GAUGE_INDICATOR = block("gauge/indicator"), GAUGE_HEAD_SPEED = block("gauge/speedometer/head"),
		GAUGE_HEAD_STRESS = block("gauge/stressometer/head"), BEARING_TOP = block("bearing/top"),
		BEARING_TOP_WOODEN = block("bearing/top_wooden"), DRILL_HEAD = block("mechanical_drill/head"),
		HARVESTER_BLADE = block("mechanical_harvester/blade"), DEPLOYER_POLE = block("deployer/pole"),
		DEPLOYER_HAND_POINTING = block("deployer/hand_pointing"),
		DEPLOYER_HAND_PUNCHING = block("deployer/hand_punching"),
		DEPLOYER_HAND_HOLDING = block("deployer/hand_holding"), ANALOG_LEVER_HANDLE = block("analog_lever/handle"),
		ANALOG_LEVER_INDICATOR = block("analog_lever/indicator"), FUNNEL_FLAP = block("funnel/flap"),
		BELT_FUNNEL_FLAP = block("belt_funnel/flap"), BELT_TUNNEL_FLAP = block("belt_tunnel/flap"),
		FLEXPEATER_INDICATOR = block("diodes/indicator"),

		CUCKOO_MINUTE_HAND = block("cuckoo_clock/minute_hand"), CUCKOO_HOUR_HAND = block("cuckoo_clock/hour_hand"),
		CUCKOO_LEFT_DOOR = block("cuckoo_clock/left_door"), CUCKOO_RIGHT_DOOR = block("cuckoo_clock/right_door"),
		CUCKOO_PIG = block("cuckoo_clock/pig"), CUCKOO_CREEPER = block("cuckoo_clock/creeper"),

		GANTRY_COGS = block("gantry_carriage/wheels"),

		ROPE_COIL = block("rope_pulley/rope_coil"), ROPE_HALF = block("rope_pulley/rope_half"),
		ROPE_HALF_MAGNET = block("rope_pulley/rope_half_magnet"),

		HOSE_COIL = block("hose_pulley/rope_coil"), HOSE = block("hose_pulley/rope"),
		HOSE_MAGNET = block("hose_pulley/pulley_magnet"), HOSE_HALF = block("hose_pulley/rope_half"),
		HOSE_HALF_MAGNET = block("hose_pulley/rope_half_magnet"),

		MILLSTONE_COG = block("millstone/inner"),

		SYMMETRY_PLANE = block("symmetry_effect/plane"), SYMMETRY_CROSSPLANE = block("symmetry_effect/crossplane"),
		SYMMETRY_TRIPLEPLANE = block("symmetry_effect/tripleplane"),

		STICKER_HEAD = block("sticker/head"),

		PORTABLE_STORAGE_INTERFACE_MIDDLE = block("portable_storage_interface/block_middle"),
		PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED = block("portable_storage_interface/block_middle_powered"),
		PORTABLE_STORAGE_INTERFACE_TOP = block("portable_storage_interface/block_top"),

		PORTABLE_FLUID_INTERFACE_MIDDLE = block("portable_fluid_interface/block_middle"),
		PORTABLE_FLUID_INTERFACE_MIDDLE_POWERED = block("portable_fluid_interface/block_middle_powered"),
		PORTABLE_FLUID_INTERFACE_TOP = block("portable_fluid_interface/block_top"),

		ARM_COG = block("mechanical_arm/cog"), ARM_BASE = block("mechanical_arm/base"),
		ARM_LOWER_BODY = block("mechanical_arm/lower_body"), ARM_UPPER_BODY = block("mechanical_arm/upper_body"),
		ARM_HEAD = block("mechanical_arm/head"), ARM_CLAW_BASE = block("mechanical_arm/claw_base"),
		ARM_CLAW_GRIP = block("mechanical_arm/claw_grip"),

		MECHANICAL_PUMP_ARROW = block("mechanical_pump/arrow"), MECHANICAL_PUMP_COG = block("mechanical_pump/cog"),
		FLUID_PIPE_CASING = block("fluid_pipe/casing"), FLUID_VALVE_POINTER = block("fluid_valve/pointer"),

		SPOUT_TOP = block("spout/top"), SPOUT_MIDDLE = block("spout/middle"), SPOUT_BOTTOM = block("spout/bottom"),

		PECULIAR_BELL = block("peculiar_bell"), HAUNTED_BELL = block("haunted_bell"),

		TOOLBOX_DRAWER = block("toolbox/drawer"),

		SPEED_CONTROLLER_BRACKET = block("rotation_speed_controller/bracket"),

		GOGGLES = block("goggles"),

		EJECTOR_TOP = block("weighted_ejector/top"),

		COPPER_BACKTANK_SHAFT = block("copper_backtank/block_shaft_input"),
		COPPER_BACKTANK_COGS = block("copper_backtank/block_cogs"),

		TRACK_SEGMENT_LEFT = block("track/segment_left"), TRACK_SEGMENT_RIGHT = block("track/segment_right"),
		TRACK_TIE = block("track/tie"), GIRDER_SEGMENT_TOP = block("metal_girder/segment_top"),
		GIRDER_SEGMENT_MIDDLE = block("metal_girder/segment_middle"),
		GIRDER_SEGMENT_BOTTOM = block("metal_girder/segment_bottom"),

		TRACK_STATION_OVERLAY = block("track_overlay/station"), TRACK_SIGNAL_OVERLAY = block("track_overlay/signal"),
		TRACK_ASSEMBLING_OVERLAY = block("track_overlay/assembling"),
		TRACK_SIGNAL_DUAL_OVERLAY = block("track_overlay/signal_dual"),
		TRACK_OBSERVER_OVERLAY = block("track_overlay/observer"),

		BOGEY_FRAME = block("track/bogey/bogey_frame"), SMALL_BOGEY_WHEELS = block("track/bogey/bogey_wheel"),
		BOGEY_PIN = block("track/bogey/bogey_drive_wheel_pin"), BOGEY_PISTON = block("track/bogey/bogey_drive_piston"),
		BOGEY_DRIVE = block("track/bogey/bogey_drive"), LARGE_BOGEY_WHEELS = block("track/bogey/bogey_drive_wheel"),

		TRAIN_COUPLING_HEAD = block("track/bogey/coupling_head"),
		TRAIN_COUPLING_CABLE = block("track/bogey/coupling_cable"),

		TRAIN_CONTROLS_COVER = block("controls/train/cover"), TRAIN_CONTROLS_LEVER = block("controls/train/lever"),

		ENGINE_PISTON = block("steam_engine/piston"), ENGINE_LINKAGE = block("steam_engine/linkage"),
		ENGINE_CONNECTOR = block("steam_engine/shaft_connector"), BOILER_GAUGE = block("steam_engine/gauge"),
		BOILER_GAUGE_DIAL = block("steam_engine/gauge_dial"),

		SIGNAL_ON = block("track_signal/indicator_on"), SIGNAL_OFF = block("track_signal/indicator_off"),
		DISPLAY_LINK_TUBE = block("display_link/tube"), DISPLAY_LINK_GLOW = block("display_link/glow"),

		STATION_ON = block("track_station/flag_on"), STATION_OFF = block("track_station/flag_off"),
		STATION_ASSEMBLE = block("track_station/flag_assemble"),

		SIGNAL_PANEL = block("track_signal/panel"), SIGNAL_WHITE_CUBE = block("track_signal/white_cube"),
		SIGNAL_WHITE_GLOW = block("track_signal/white_glow"), SIGNAL_WHITE = block("track_signal/white_tube"),
		SIGNAL_RED_CUBE = block("track_signal/red_cube"), SIGNAL_RED_GLOW = block("track_signal/red_glow"),
		SIGNAL_RED = block("track_signal/red_tube"), SIGNAL_YELLOW_CUBE = block("track_signal/yellow_cube"),
		SIGNAL_YELLOW_GLOW = block("track_signal/yellow_glow"), SIGNAL_YELLOW = block("track_signal/yellow_tube"),

		BLAZE_INERT = block("blaze_burner/blaze/inert"), BLAZE_SUPER_ACTIVE = block("blaze_burner/blaze/super_active"),
		BLAZE_GOGGLES = block("blaze_burner/goggles"), BLAZE_GOGGLES_SMALL = block("blaze_burner/goggles_small"),
		BLAZE_IDLE = block("blaze_burner/blaze/idle"), BLAZE_ACTIVE = block("blaze_burner/blaze/active"),
		BLAZE_SUPER = block("blaze_burner/blaze/super"), BLAZE_BURNER_FLAME = block("blaze_burner/flame"),
		BLAZE_BURNER_RODS = block("blaze_burner/rods_small"),
		BLAZE_BURNER_RODS_2 = block("blaze_burner/rods_large"),
		BLAZE_BURNER_SUPER_RODS = block("blaze_burner/superheated_rods_small"),
		BLAZE_BURNER_SUPER_RODS_2 = block("blaze_burner/superheated_rods_large"),

		WHISTLE_MOUTH_LARGE = block("steam_whistle/large_mouth"),
		WHISTLE_MOUTH_MEDIUM = block("steam_whistle/medium_mouth"),
		WHISTLE_MOUTH_SMALL = block("steam_whistle/small_mouth"),

		CRAFTING_BLUEPRINT_1x1 = entity("crafting_blueprint_small"),
		CRAFTING_BLUEPRINT_2x2 = entity("crafting_blueprint_medium"),
		CRAFTING_BLUEPRINT_3x3 = entity("crafting_blueprint_large"),

		TRAIN_HAT = entity("train_hat"),

		COUPLING_ATTACHMENT = entity("minecart_coupling/attachment"), COUPLING_RING = entity("minecart_coupling/ring"),
		COUPLING_CONNECTOR = entity("minecart_coupling/connector")

	;

	public static final Map<FluidTransportBehaviour.AttachmentTypes.ComponentPartials, Map<Direction, PartialModel>> PIPE_ATTACHMENTS =
		new EnumMap<>(FluidTransportBehaviour.AttachmentTypes.ComponentPartials.class);

	public static final Map<Direction, PartialModel> METAL_GIRDER_BRACKETS = new EnumMap<>(Direction.class);
	public static final Map<DyeColor, PartialModel> TOOLBOX_LIDS = new EnumMap<>(DyeColor.class);

	static {
		for (FluidTransportBehaviour.AttachmentTypes.ComponentPartials type : FluidTransportBehaviour.AttachmentTypes.ComponentPartials.values()) {
			Map<Direction, PartialModel> map = new HashMap<>();
			for (Direction d : Iterate.directions) {
				String asId = Lang.asId(type.name());
				map.put(d, block("fluid_pipe/" + asId + "/" + Lang.asId(d.getSerializedName())));
			}
			PIPE_ATTACHMENTS.put(type, map);
		}
		for (DyeColor color : DyeColor.values())
			TOOLBOX_LIDS.put(color, block("toolbox/lid/" + Lang.asId(color.name())));
		for (Direction d : Iterate.horizontalDirections)
			METAL_GIRDER_BRACKETS.put(d, block("metal_girder/bracket_" + Lang.asId(d.name())));
	}

	private static PartialModel block(String path) {
		return new PartialModel(Create.asResource("block/" + path));
	}

	private static PartialModel entity(String path) {
		return new PartialModel(Create.asResource("entity/" + path));
	}

	public static void init() {
		// init static fields
	}

}
