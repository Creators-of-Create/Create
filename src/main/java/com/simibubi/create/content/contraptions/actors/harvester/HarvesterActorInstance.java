package com.simibubi.create.content.contraptions.actors.harvester;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public class HarvesterActorInstance extends ActorInstance {
    static float originOffset = 1 / 16f;
    static Vec3 rotOffset = new Vec3(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);

    protected TransformedInstance harvester;
    private Direction facing;

    protected float horizontalAngle;

    private double rotation;
    private double previousRotation;

    public HarvesterActorInstance(VisualizationContext materialManager, VirtualRenderWorld simulationWorld, MovementContext context) {
        super(materialManager, simulationWorld, context);

        BlockState state = context.state;

        facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        harvester = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(getRollingPartial()), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();

        horizontalAngle = facing.toYRot() + ((facing.getAxis() == Direction.Axis.X) ? 180 : 0);

		harvester.setBlockLight(localBlockLight());
	}

	protected PartialModel getRollingPartial() {
		return AllPartialModels.HARVESTER_BLADE;
	}

	protected Vec3 getRotationOffset() {
		return rotOffset;
	}

	protected double getRadius() {
		return 6.5;
	}

	@Override
	public void tick() {
		super.tick();

		previousRotation = rotation;

		if (context.contraption.stalled || context.disabled
			|| VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
			return;

		double arcLength = context.motion.length();

		double radians = arcLength * 16 / getRadius();

		float deg = AngleHelper.deg(radians);

		deg = (float) (((int) (deg * 3000)) / 3000);

		rotation += deg * 1.25;

		rotation %= 360;
	}

    @Override
    public void beginFrame() {
        harvester.loadIdentity()
				.translate(context.localPos)
				.center()
				.rotateYDegrees(horizontalAngle)
				.uncenter()
				.translate(getRotationOffset())
				.rotateXDegrees((float) getRotation())
				.translateBack(getRotationOffset());
	}

    protected double getRotation() {
        return AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), previousRotation, rotation);
    }
}
