package com.simibubi.create;

import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.Map;

import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

public enum AllBlockPartials {

	SCHEMATICANNON_CONNECTOR("schematicannon/connector"),
	SCHEMATICANNON_PIPE("schematicannon/pipe"),

	SHAFTLESS_COGWHEEL("cogwheel_shaftless"),
	BELT_PULLEY,
	SHAFT_HALF,

	ENCASED_FAN_INNER("encased_fan/propeller"),
	HAND_CRANK_HANDLE("hand_crank/handle"),
	MECHANICAL_PRESS_HEAD,
	MECHANICAL_MIXER_POLE("mixer_pole"),
	MECHANICAL_MIXER_HEAD("mixer_head"),
	MECHANICAL_CRAFTER_LID("crafter/lid"),
	MECHANICAL_CRAFTER_ARROW("crafter/arrow"),
	MECHANICAL_CRAFTER_BELT_FRAME("crafter/belt"),
	MECHANICAL_CRAFTER_BELT("crafter/belt_animated"),
	GAUGE_DIAL("gauge/dial"),
	GAUGE_INDICATOR("gauge/indicator"),
	GAUGE_HEAD_SPEED("gauge/speed"),
	GAUGE_HEAD_STRESS("gauge/stress"),
	MECHANICAL_BEARING_TOP("bearing/top"),
	DRILL,
	HARVESTER_BLADE,
	DEPLOYER_POLE("deployer/pole"),
	DEPLOYER_HAND_POINTING("deployer/hand_pointing"),
	DEPLOYER_HAND_PUNCHING("deployer/hand_punching"),
	DEPLOYER_HAND_HOLDING("deployer/hand_holding"),
	ANALOG_LEVER_HANDLE("analog_lever/handle"),
	ANALOG_LEVER_INDICATOR("analog_lever/indicator"),
	BELT_TUNNEL_FLAP("belt_tunnel/flap"),
	BELT_TUNNEL_INDICATOR("belt_tunnel/indicator"),
	FLEXPEATER_INDICATOR("repeaters/flexpeater_indicator"),
	FLYWHEEL("flywheel/wheel"),
	FLYWHEEL_UPPER_ROTATING("flywheel/upper_rotating_connector"),
	FLYWHEEL_LOWER_ROTATING("flywheel/lower_rotating_connector"),
	FLYWHEEL_UPPER_SLIDING("flywheel/upper_sliding_connector"),
	FLYWHEEL_LOWER_SLIDING("flywheel/lower_sliding_connector"),
	FURNACE_GENERATOR_FRAME("furnace_engine/frame"),
	CUCKOO_MINUTE_HAND("cuckoo_clock/minute_hand"),
	CUCKOO_HOUR_HAND("cuckoo_clock/hour_hand"),
	CUCKOO_LEFT_DOOR("cuckoo_clock/left_door"),
	CUCKOO_RIGHT_DOOR("cuckoo_clock/right_door"),
	CUCKOO_PIG("cuckoo_clock/pig"),
	CUCKOO_CREEPER("cuckoo_clock/creeper"),
	ROPE_COIL("pulley/rope_coil"),
	ROPE_HALF("pulley/rope_half"),
	ROPE_HALF_MAGNET("pulley/rope_half_magnet"),
	MILL_STONE_COG("millstone/inner"),
	
	;

	private ResourceLocation modelLocation;
	private IBakedModel bakedModel;

	private AllBlockPartials() {
	}

	private AllBlockPartials(String path) {
		modelLocation = new ResourceLocation(Create.ID, "block/" + path);
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		for (AllBlockPartials partial : AllBlockPartials.values()) {
			partial.createModelLocation();
			ModelLoader.addSpecialModel(partial.modelLocation);
		}
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		for (AllBlockPartials partial : AllBlockPartials.values()) {
			partial.createModelLocation();
			partial.bakedModel = modelRegistry.get(partial.modelLocation);
		}
	}

	private void createModelLocation() {
		if (modelLocation == null)
			modelLocation = new ResourceLocation(Create.ID, "block/" + Lang.asId(name()));
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
		SuperByteBuffer renderPartial = CreateClient.bufferCache.renderPartial(this, referenceState);
		renderPartial.rotateCentered(Axis.X, AngleHelper.rad(AngleHelper.verticalAngle(facing)));
		renderPartial.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(facing)));
		return renderPartial;
	}

}
