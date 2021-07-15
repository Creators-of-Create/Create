package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.backend.instancing.InstanceMaterial;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;

public class DrillActorInstance extends ActorInstance {

    ActorData drillHead;
    private final Direction facing;

    public DrillActorInstance(MaterialManager<?> materialManager, PlacementSimulationWorld contraption, MovementContext context) {
        super(materialManager, contraption, context);

        InstanceMaterial<ActorData> instanceMaterial = materialManager.getMaterial(AllMaterialSpecs.ACTORS);

        BlockState state = context.state;

        facing = state.getValue(DrillBlock.FACING);

        Direction.Axis axis = facing.getAxis();
        float eulerX = AngleHelper.verticalAngle(facing);

        float eulerY;
        if (axis == Direction.Axis.Y)
            eulerY = 0;
        else
            eulerY = facing.toYRot() + ((axis == Direction.Axis.X) ? 180 : 0);

        drillHead = instanceMaterial.getModel(AllBlockPartials.DRILL_HEAD, state).createInstance();

        drillHead.setPosition(context.localPos)
                 .setBlockLight(localBlockLight())
                 .setRotationOffset(0)
                 .setRotationAxis(0, 0, 1)
                 .setLocalRotation(new Quaternion(eulerX, eulerY, 0, true))
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
