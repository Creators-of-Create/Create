package com.simibubi.create.content.contraptions.components.actors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionKineticRenderer;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class HarvesterActorInstance extends ActorInstance {
    static double oneOverRadius = 16.0 / 6.5;
    static float originOffset = 1 / 16f;
    static Vector3d rotOffset = new Vector3d(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);


    InstanceKey<ModelData> harvester;
    private Direction facing;

    private float horizontalAngle;

    private double rotation;
    private double previousRotation;

    public HarvesterActorInstance(ContraptionKineticRenderer modelManager, MovementContext context) {
        super(modelManager, context);

        RenderMaterial<?, InstancedModel<ModelData>> renderMaterial = modelManager.transformMaterial();

        BlockState state = context.state;

        facing = state.get(HORIZONTAL_FACING);

        harvester = renderMaterial.getModel(AllBlockPartials.HARVESTER_BLADE, state).createInstance();

        horizontalAngle = facing.getHorizontalAngle() + ((facing.getAxis() == Direction.Axis.X) ? 180 : 0);

        harvester.getInstance()
                 .setBlockLight(localBlockLight());
    }

    @Override
    public void tick() {
        super.tick();

        previousRotation = rotation;

        if (context.contraption.stalled || VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite()))
            return;

        double arcLength = context.motion.length();

        double radians = arcLength * oneOverRadius;

        float deg = AngleHelper.deg(radians);

        deg = (float) (((int) (deg * 3000)) / 3000);

        rotation += deg * 1.25;

        rotation %= 360;
    }

    @Override
    public void beginFrame() {
        MatrixStack ms = new MatrixStack();
        MatrixStacker msr = MatrixStacker.of(ms);

        msr.translate(context.localPos)
           .centre()
           .rotateY(horizontalAngle)
           .unCentre()
           .translate(rotOffset)
           .rotateX(getRotation())
           .translateBack(rotOffset);

        harvester.getInstance().setTransform(ms);
    }

    private double getRotation() {
        return AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), previousRotation, rotation);
    }
}
