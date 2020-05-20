package com.simibubi.create;

import static net.minecraft.state.properties.BlockStateProperties.FACING;
import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
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
	MECHANICAL_PRESS_HEAD("mechanical_press/head"),
	MECHANICAL_MIXER_POLE("mechanical_mixer/pole"),
	MECHANICAL_MIXER_HEAD("mechanical_mixer/head"),
	MECHANICAL_CRAFTER_LID("mechanical_crafter/lid"),
	MECHANICAL_CRAFTER_ARROW("mechanical_crafter/arrow"),
	MECHANICAL_CRAFTER_BELT_FRAME("mechanical_crafter/belt"),
	MECHANICAL_CRAFTER_BELT("mechanical_crafter/belt_animated"),
	GAUGE_DIAL("gauge/dial"),
	GAUGE_INDICATOR("gauge/indicator"),
	GAUGE_HEAD_SPEED("gauge/speedometer/head"),
	GAUGE_HEAD_STRESS("gauge/stressometer/head"),
	BEARING_TOP("bearing/top"),
	DRILL_HEAD("drill/head"),
	HARVESTER_BLADE("harvester/blade"),
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
	ROPE_COIL("rope_pulley/rope_coil"),
	ROPE_HALF("rope_pulley/rope_half"),
	ROPE_HALF_MAGNET("rope_pulley/rope_half_magnet"),
	MILLSTONE_COG("millstone/inner"),
	
	SYMMETRY_PLANE("symmetry_effect/plane"),
	SYMMETRY_CROSSPLANE("symmetry_effect/crossplane"),
	SYMMETRY_TRIPLEPLANE("symmetry_effect/tripleplane"),
	
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
		MatrixStack ms = new MatrixStack();
		// TODO 1.15 find a way to cache this model matrix computation
		ms.translate(0.5, 0.5, 0.5);
		ms.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(AngleHelper.rad(AngleHelper.horizontalAngle(facing))));
		ms.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(AngleHelper.rad(AngleHelper.verticalAngle(facing))));
		ms.translate(-0.5, -0.5, -0.5);
		SuperByteBuffer renderPartial = CreateClient.bufferCache.renderDirectionalPartial(this, referenceState, facing, ms);
		return renderPartial;
	}

}
