package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.world.phys.Vec3;

public class RollerActorInstance extends HarvesterActorInstance {

	static Vec3 rotOffset = new Vec3(0.5f, -12 * originOffset + 0.5f, 8 * originOffset + 0.5f);

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
		super.beginFrame();
		frame.loadIdentity()
			.translate(context.localPos)
			.centre()
			.rotateY(horizontalAngle)
			.unCentre();
	}

	@Override
	protected PartialModel getRollingPartial() {
		return AllPartialModels.ROLLER_WHEEL;
	}

	@Override
	protected Vec3 getRotationOffset() {
		return rotOffset;
	}

	@Override
	protected double getRadius() {
		return 16.5;
	}

}
