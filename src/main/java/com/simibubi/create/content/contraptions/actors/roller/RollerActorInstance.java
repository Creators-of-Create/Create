package com.simibubi.create.content.contraptions.actors.roller;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterActorInstance;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.world.phys.Vec3;

public class RollerActorInstance extends HarvesterActorInstance {

	ModelData frame;

	public RollerActorInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		super(materialManager, simulationWorld, context);

		Material<ModelData> material = materialManager.defaultCutout()
			.material(Materials.TRANSFORMED);
		frame = material.getModel(AllPartialModels.ROLLER_FRAME, context.state)
			.createInstance();
		frame.setBlockLight(localBlockLight());
	}

	@Override
	public void beginFrame() {
		harvester.loadIdentity()
			.translate(context.localPos)
			.centre()
			.rotateY(horizontalAngle)
			.unCentre()
			.translate(0, -.25, 17 / 16f)
			.rotateX(getRotation())
			.translate(0, -.5, .5)
			.rotateY(90);

		frame.loadIdentity()
			.translate(context.localPos)
			.centre()
			.rotateY(horizontalAngle + 180)
			.unCentre();
	}

	@Override
	protected PartialModel getRollingPartial() {
		return AllPartialModels.ROLLER_WHEEL;
	}

	@Override
	protected Vec3 getRotationOffset() {
		return Vec3.ZERO;
	}

	@Override
	protected double getRadius() {
		return 16.5;
	}

}
