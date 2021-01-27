package com.simibubi.create.content.contraptions.components.actors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.contraption.RenderedContraption;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RenderMaterial;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.render.instancing.actors.StaticRotatingActorData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class DrillRenderer extends KineticTileEntityRenderer {

	public DrillRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected InstancedModel<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouthRotating(ctx, ctx.te.getBlockState());
	}

	protected static SuperByteBuffer getRotatingModel(BlockState state) {
		return AllBlockPartials.DRILL_HEAD.renderOnDirectionalSouth(state);
	}

	public static void addInstanceForContraption(RenderedContraption contraption, MovementContext context) {
		RenderMaterial<InstancedModel<StaticRotatingActorData>> renderMaterial = contraption.getActorMaterial();

		BlockState state = context.state;
		InstancedModel<StaticRotatingActorData> model = renderMaterial.getModel(AllBlockPartials.DRILL_HEAD, state);

		model.setupInstance(data -> {
			Direction facing = state.get(DrillBlock.FACING);
			float eulerX = AngleHelper.verticalAngle(facing) + ((facing.getAxis() == Direction.Axis.Y) ? 180 : 0);
			float eulerY = facing.getHorizontalAngle();
			data.setPosition(context.localPos)
				.setRotationOffset(0)
				.setRotationAxis(0, 0, 1)
				.setLocalRotation(eulerX, eulerY, 0);
		});
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
		BlockState state = context.state;
		SuperByteBuffer superBuffer = AllBlockPartials.DRILL_HEAD.renderOn(state);
		Direction facing = state.get(DrillBlock.FACING);
		
		float speed = (float) (context.contraption.stalled
			|| !VecHelper.isVecPointingTowards(context.relativeMotion, state.get(FACING)
				.getOpposite()) ? context.getAnimationSpeed() : 0);
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (float) (((time * speed) % 360));

		for (MatrixStack m : matrixStacks)
			MatrixStacker.of(m)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing))
				.rotateZ(angle)
				.unCentre();
		
		superBuffer
			.light(msLocal.peek()
			.getModel())
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}