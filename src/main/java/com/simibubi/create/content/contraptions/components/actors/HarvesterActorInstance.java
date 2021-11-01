package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.jozufozu.flywheel.backend.material.Material;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class HarvesterActorInstance extends ActorInstance {
    static double oneOverRadius = 16.0 / 6.5;
    static float originOffset = 1 / 16f;
    static Vector3d rotOffset = new Vector3d(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);


    ModelData harvester;
    private Direction facing;

    private float horizontalAngle;

    private double rotation;
    private double previousRotation;

    public HarvesterActorInstance(MaterialManager materialManager, PlacementSimulationWorld simulationWorld, MovementContext context) {
        super(materialManager, simulationWorld, context);

		Material<ModelData> material = materialManager.defaultCutout()
				.material(Materials.TRANSFORMED);

        BlockState state = context.state;

        facing = state.getValue(HORIZONTAL_FACING);

        harvester = material.getModel(AllBlockPartials.HARVESTER_BLADE, state).createInstance();

        horizontalAngle = facing.toYRot() + ((facing.getAxis() == Direction.Axis.X) ? 180 : 0);

        harvester.setBlockLight(localBlockLight());
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
        MatrixTransformStack msr = MatrixTransformStack.of(ms);

        msr.translate(context.localPos)
           .centre()
           .rotateY(horizontalAngle)
           .unCentre()
           .translate(rotOffset)
           .rotateX(getRotation())
           .translateBack(rotOffset);

        harvester.setTransform(ms);
    }

    private double getRotation() {
        return AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), previousRotation, rotation);
    }
}
