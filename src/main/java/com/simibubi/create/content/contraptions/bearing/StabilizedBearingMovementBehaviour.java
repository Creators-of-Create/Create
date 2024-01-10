package com.simibubi.create.content.contraptions.bearing;

import javax.annotation.Nullable;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StabilizedBearingMovementBehaviour implements MovementBehaviour {

	@Override
	public ItemStack canBeDisabledVia(MovementContext context) {
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		if (ContraptionRenderDispatcher.canInstance())
			return;

		Direction facing = context.state.getValue(BlockStateProperties.FACING);
		PartialModel top = AllPartialModels.BEARING_TOP;
		SuperByteBuffer superBuffer = CachedBufferer.partial(top, context.state);
		float renderPartialTicks = AnimationTickHolder.getPartialTicks();

		// rotate to match blockstate
		Quaternionf orientation = BearingInstance.getBlockStateOrientation(facing);

		// rotate against parent
		float angle = getCounterRotationAngle(context, facing, renderPartialTicks) * facing.getAxisDirection()
			.getStep();

		Quaternionf rotation = Axis.of(facing.step())
			.rotationDegrees(angle);

		rotation.mul(orientation);

		orientation = rotation;

		superBuffer.transform(matrices.getModel());
		superBuffer.rotateCentered(orientation);

		// render
		superBuffer
			.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(VisualizationContext materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		return new StabilizedBearingInstance(materialManager, simulationWorld, context);
	}

	static float getCounterRotationAngle(MovementContext context, Direction facing, float renderPartialTicks) {
		if (!context.contraption.canBeStabilized(facing, context.localPos))
			return 0;

		float offset = 0;
		Direction.Axis axis = facing.getAxis();
		AbstractContraptionEntity entity = context.contraption.entity;

		if (entity instanceof ControlledContraptionEntity) {
			ControlledContraptionEntity controlledCE = (ControlledContraptionEntity) entity;
			if (context.contraption.canBeStabilized(facing, context.localPos))
				offset = -controlledCE.getAngle(renderPartialTicks);

		} else if (entity instanceof OrientedContraptionEntity) {
			OrientedContraptionEntity orientedCE = (OrientedContraptionEntity) entity;
			if (axis.isVertical())
				offset = -orientedCE.getViewYRot(renderPartialTicks);
			else {
				if (orientedCE.isInitialOrientationPresent() && orientedCE.getInitialOrientation()
					.getAxis() == axis)
					offset = -orientedCE.getViewXRot(renderPartialTicks);
			}
		}
		return offset;
	}

}
