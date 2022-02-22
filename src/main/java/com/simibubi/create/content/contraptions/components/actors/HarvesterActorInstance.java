package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class HarvesterActorInstance extends ActorInstance {
    static double oneOverRadius = 16.0 / 6.5;
    static float originOffset = 1 / 16f;
    static Vec3 rotOffset = new Vec3(0.5f, -2 * originOffset + 0.5f, originOffset + 0.5f);


    ModelData harvester;
    private Direction facing;

    private float horizontalAngle;

    private double rotation;
    private double previousRotation;

    public HarvesterActorInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld, MovementContext context) {
        super(materialManager, simulationWorld, context);

		Material<ModelData> material = materialManager.defaultCutout()
				.material(Materials.TRANSFORMED);

        BlockState state = context.state;

        facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

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
        harvester.loadIdentity()
				.translate(context.localPos)
				.centre()
				.rotateY(horizontalAngle)
				.unCentre()
				.translate(rotOffset)
				.rotateX(getRotation())
				.translateBack(rotOffset);
	}

    private double getRotation() {
        return AngleHelper.angleLerp(AnimationTickHolder.getPartialTicks(), previousRotation, rotation);
    }
}
