package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class HarvesterActorInstance extends ActorInstance {

    InstanceKey<ContraptionActorData> harvester;
    private Direction facing;

    public HarvesterActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        super(modelManager, context);

        RenderMaterial<?, InstancedModel<ContraptionActorData>> renderMaterial = modelManager.getActorMaterial();

        BlockState state = context.state;

        facing = state.get(HORIZONTAL_FACING);
        float originOffset = 1 / 16f;
        Vector3f rotOffset = new Vector3f(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);

        harvester = renderMaterial.getModel(AllBlockPartials.HARVESTER_BLADE, state).createInstance();

        float horizontalAngle = facing.getHorizontalAngle() + ((facing.getAxis() == Direction.Axis.X) ? 180 : 0);
        harvester.getInstance()
                 .setPosition(context.localPos)
                 .setBlockLight(localBlockLight())
                 .setRotationOffset(0)
                 .setRotationCenter(rotOffset)
                 .setRotationAxis(-1, 0, 0)
                 .setLocalRotation(new Quaternion(Vector3f.POSITIVE_Y, horizontalAngle, true))
                 .setSpeed(getSpeed(facing));
    }

    @Override
    protected void tick() {
        harvester.getInstance().setSpeed(getSpeed(facing));
    }
}
