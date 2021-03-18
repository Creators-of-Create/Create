package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class DrillActorInstance extends ActorInstance {

    InstanceKey<ContraptionActorData> drillHead;
    private Direction facing;

    public DrillActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        super(modelManager, context);

        RenderMaterial<?, InstancedModel<ContraptionActorData>> renderMaterial = modelManager.getActorMaterial();

        BlockState state = context.state;

        facing = state.get(DrillBlock.FACING);
        float eulerX = AngleHelper.verticalAngle(facing) + ((facing.getAxis() == Direction.Axis.Y) ? 180 : 0);
        float eulerY = facing.getHorizontalAngle();

        drillHead = renderMaterial.getModel(AllBlockPartials.DRILL_HEAD, state).createInstance();

        drillHead.getInstance()
                 .setPosition(context.localPos)
                 .setBlockLight(localBlockLight())
                 .setRotationOffset(0)
                 .setRotationAxis(0, 0, 1)
                 .setLocalRotation(eulerX, eulerY, 0)
                 .setSpeed(getSpeed(facing));
    }

    @Override
    protected void tick() {
        drillHead.getInstance().setSpeed(getSpeed(facing));
    }

    @Override
    protected float getSpeed(Direction facing) {
        if (context.contraption.stalled || !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
            return context.getAnimationSpeed();
        return 0;
    }
}
