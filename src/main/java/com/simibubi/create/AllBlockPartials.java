package com.simibubi.create;

import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.fluids.FluidPipeAttachmentBehaviour.AttachmentTypes;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

public class AllBlockPartials {

	private static final List<AllBlockPartials> all = new ArrayList<>();

	public static final AllBlockPartials SCHEMATICANNON_CONNECTOR = get("schematicannon/connector"),
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
		MECHANICAL_CRAFTER_BELT = get("mechanical_crafter/belt_animated"), GAUGE_DIAL = get("gauge/dial"),
		GAUGE_INDICATOR = get("gauge/indicator"), GAUGE_HEAD_SPEED = get("gauge/speedometer/head"),
		GAUGE_HEAD_STRESS = get("gauge/stressometer/head"), BEARING_TOP = get("bearing/top"),
		DRILL_HEAD = get("mechanical_drill/head"), HARVESTER_BLADE = get("mechanical_harvester/blade"),
		DEPLOYER_POLE = get("deployer/pole"), DEPLOYER_HAND_POINTING = get("deployer/hand_pointing"),
		DEPLOYER_HAND_PUNCHING = get("deployer/hand_punching"), DEPLOYER_HAND_HOLDING = get("deployer/hand_holding"),
		ANALOG_LEVER_HANDLE = get("analog_lever/handle"), ANALOG_LEVER_INDICATOR = get("analog_lever/indicator"),
		BELT_FUNNEL_FLAP = get("belt_funnel/flap"), BELT_TUNNEL_FLAP = get("belt_tunnel/flap"),
		FLEXPEATER_INDICATOR = get("diodes/indicator"), FLYWHEEL = get("flywheel/wheel"),
		FLYWHEEL_UPPER_ROTATING = get("flywheel/upper_rotating_connector"),

		FLYWHEEL_LOWER_ROTATING = get("flywheel/lower_rotating_connector"),
		FLYWHEEL_UPPER_SLIDING = get("flywheel/upper_sliding_connector"),
		FLYWHEEL_LOWER_SLIDING = get("flywheel/lower_sliding_connector"),
		FURNACE_GENERATOR_FRAME = get("furnace_engine/frame"), CUCKOO_MINUTE_HAND = get("cuckoo_clock/minute_hand"),
		CUCKOO_HOUR_HAND = get("cuckoo_clock/hour_hand"), CUCKOO_LEFT_DOOR = get("cuckoo_clock/left_door"),
		CUCKOO_RIGHT_DOOR = get("cuckoo_clock/right_door"), CUCKOO_PIG = get("cuckoo_clock/pig"),
		CUCKOO_CREEPER = get("cuckoo_clock/creeper"), ROPE_COIL = get("rope_pulley/rope_coil"),
		ROPE_HALF = get("rope_pulley/rope_half"), ROPE_HALF_MAGNET = get("rope_pulley/rope_half_magnet"),
		MILLSTONE_COG = get("millstone/inner"), PACKAGER_SEALER = get("packager/sealer"),

		SYMMETRY_PLANE = get("symmetry_effect/plane"), SYMMETRY_CROSSPLANE = get("symmetry_effect/crossplane"),
		SYMMETRY_TRIPLEPLANE = get("symmetry_effect/tripleplane"),

		ARM_COG = get("mechanical_arm/cog"), ARM_BASE = get("mechanical_arm/base"),
		ARM_LOWER_BODY = get("mechanical_arm/lower_body"), ARM_UPPER_BODY = get("mechanical_arm/upper_body"),
		ARM_HEAD = get("mechanical_arm/head"), ARM_CLAW_BASE = get("mechanical_arm/claw_base"),
		ARM_CLAW_GRIP = get("mechanical_arm/claw_grip"),

		FLAG_SHORT_IN = get("mechanical_arm/flag/short_in"), FLAG_SHORT_OUT = get("mechanical_arm/flag/short_out"),
		FLAG_LONG_IN = get("mechanical_arm/flag/long_in"), FLAG_LONG_OUT = get("mechanical_arm/flag/long_out"),

		MECHANICAL_PUMP_ARROW = get("mechanical_pump/arrow"), MECHANICAL_PUMP_COG = get("mechanical_pump/cog"),
		FLUID_PIPE_CASING = get("fluid_pipe/casing"),
		
		SPOUT_TOP = get("spout/top"),
		SPOUT_MIDDLE = get("spout/middle"),
		SPOUT_BOTTOM = get("spout/bottom"),

		COUPLING_ATTACHMENT = getEntity("minecart_coupling/attachment"),
		COUPLING_RING = getEntity("minecart_coupling/ring"),
		COUPLING_CONNECTOR = getEntity("minecart_coupling/connector")

	;

	public static final Map<AttachmentTypes, Map<Direction, AllBlockPartials>> PIPE_ATTACHMENTS = map();
	public static final Map<HeatLevel, AllBlockPartials> BLAZES = map();

	static {
		populateMaps();
	}

	;

	private ResourceLocation modelLocation;
	private IBakedModel bakedModel;

	private AllBlockPartials() {}

	private static void populateMaps() {
		for (AttachmentTypes type : AttachmentTypes.values()) {
			if (!type.hasModel())
				continue;
			Map<Direction, AllBlockPartials> map = map();
			for (Direction d : Iterate.directions) {
				String asId = Lang.asId(type.name());
				map.put(d, get("fluid_pipe/" + asId + "/" + Lang.asId(d.getName())));
			}
			PIPE_ATTACHMENTS.put(type, map);
		}
		for (HeatLevel heat : HeatLevel.values()) {
			if (heat == HeatLevel.NONE)
				continue;
			BLAZES.put(heat, get("blaze_burner/blaze/" + heat.getName()));
		}
	}

	private static <T, U> Map<T, U> map() {
		return new HashMap<>();
	}

	private static AllBlockPartials getEntity(String path) {
		AllBlockPartials partials = new AllBlockPartials();
		partials.modelLocation = new ResourceLocation(Create.ID, "entity/" + path);
		all.add(partials);
		return partials;
	}

	private static AllBlockPartials get(String path) {
		AllBlockPartials partials = new AllBlockPartials();
		partials.modelLocation = new ResourceLocation(Create.ID, "block/" + path);
		all.add(partials);
		return partials;
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		for (AllBlockPartials partial : all)
			ModelLoader.addSpecialModel(partial.modelLocation);
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		for (AllBlockPartials partial : all)
			partial.bakedModel = modelRegistry.get(partial.modelLocation);
	}

	public IBakedModel get() {
		return bakedModel;
	}

	public SuperByteBuffer renderOn(BlockState referenceState) {
		return CreateClient.bufferCache.renderPartial(this, referenceState);
	}

	public SuperByteBuffer renderOnDirectional(BlockState referenceState) {
		Direction facing = referenceState.get(FACING);
		return renderOnDirectional(referenceState, facing);
	}

	public SuperByteBuffer renderOnHorizontal(BlockState referenceState) {
		Direction facing = referenceState.get(HORIZONTAL_FACING);
		return renderOnDirectional(referenceState, facing);
	}

	public SuperByteBuffer renderOnDirectional(BlockState referenceState, Direction facing) {
		MatrixStack ms = new MatrixStack();
		// TODO 1.15 find a way to cache this model matrix computation
		MatrixStacker.of(ms)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing))
			.unCentre();
		return CreateClient.bufferCache.renderDirectionalPartial(this, referenceState, facing, ms);
	}

}
