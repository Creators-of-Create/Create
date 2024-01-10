package com.simibubi.create.content.kinetics.drill;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.actors.flwdata.ActorInstance;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class DrillActorInstance extends com.simibubi.create.content.contraptions.render.ActorInstance {

    ActorInstance drillHead;
    private final Direction facing;

    public DrillActorInstance(VisualizationContext materialManager, VirtualRenderWorld contraption, MovementContext context) {
        super(materialManager, contraption, context);

        BlockState state = context.state;

        facing = state.getValue(DrillBlock.FACING);

        Direction.Axis axis = facing.getAxis();
        float eulerX = AngleHelper.verticalAngle(facing);

        float eulerY;
        if (axis == Direction.Axis.Y)
            eulerY = 0;
        else
            eulerY = facing.toYRot() + ((axis == Direction.Axis.X) ? 180 : 0);

		drillHead = materialManager.instancerProvider()
				.instancer(AllInstanceTypes.ACTORS, Models.partial(AllPartialModels.DRILL_HEAD), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();

        drillHead.setPosition(context.localPos)
                 .setBlockLight(localBlockLight())
                 .setRotationOffset(0)
                 .setRotationAxis(0, 0, 1)
                 .setLocalRotation(new Quaternionf().rotationXYZ(eulerX * Mth.DEG_TO_RAD, eulerY * Mth.DEG_TO_RAD, 0))
                 .setSpeed(getSpeed(facing));
    }

    @Override
    public void beginFrame() {
        drillHead.setSpeed(getSpeed(facing));
    }

    protected float getSpeed(Direction facing) {
        if (context.contraption.stalled || !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
            return context.getAnimationSpeed();
        return 0;
    }
}
