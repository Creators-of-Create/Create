package com.simibubi.create.content.contraptions.actors.roller;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterActorInstance;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.render.VirtualRenderWorld;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public class RollerActorInstance extends HarvesterActorInstance {

	TransformedInstance frame;

	public RollerActorInstance(VisualizationContext materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		super(materialManager, simulationWorld, context);

		Material<ModelData> material = materialManager.defaultCutout()
			.material(InstanceTypes.TRANSFORMED);
		frame = material.getModel(AllPartialModels.ROLLER_FRAME, context.state)
			.createInstance();
		frame.setBlockLight(localBlockLight());
	}

	@Override
	public void beginFrame() {
		harvester.loadIdentity()
			.translate(context.localPos)
			.center()
			.rotateY(horizontalAngle)
			.uncenter()
			.translate(0, -.25, 17 / 16f)
			.rotateX(getRotation())
			.translate(0, -.5, .5)
			.rotateY(90);

		frame.loadIdentity()
			.translate(context.localPos)
			.center()
			.rotateY(horizontalAngle + 180)
			.uncenter();
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
