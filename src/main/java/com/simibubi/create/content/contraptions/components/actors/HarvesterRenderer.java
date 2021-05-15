package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HarvesterRenderer extends SafeTileEntityRenderer<HarvesterTileEntity> {

	public HarvesterRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(HarvesterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		SuperByteBuffer superBuffer = PartialBufferer.get(AllBlockPartials.HARVESTER_BLADE, blockState);
		transform(te.getWorld(), blockState.get(HarvesterBlock.HORIZONTAL_FACING), superBuffer,
				te.manuallyAnimatedSpeed);
		superBuffer.light(light)
				.renderInto(ms, buffer.getBuffer(RenderType.getCutoutMipped()));
	}

	public static void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		MatrixStack ms, MatrixStack msLocal, IRenderTypeBuffer buffers) {
		BlockState blockState = context.state;
		Direction facing = blockState.get(HORIZONTAL_FACING);
		SuperByteBuffer superBuffer = PartialBufferer.get(AllBlockPartials.HARVESTER_BLADE, blockState);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
				? context.getAnimationSpeed()
				: 0);
		if (context.contraption.stalled)
			speed = 0;

		transform(context.world, facing, superBuffer, speed);

		superBuffer
			.light(msLocal.peek().getModel(),
					ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(ms, buffers.getBuffer(RenderType.getCutoutMipped()));
	}

	public static void transform(World world, Direction facing, SuperByteBuffer superBuffer, float speed) {
		float originOffset = 1 / 16f;
		Vector3d rotOffset = new Vector3d(0, -2 * originOffset, originOffset).add(VecHelper.getCenterOf(BlockPos.ZERO));
		float time = AnimationTickHolder.getRenderTime(world) / 20;
		float angle = (time * speed) % 360;

		superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.translate(rotOffset.x, rotOffset.y, rotOffset.z)
			.rotate(Direction.WEST, AngleHelper.rad(angle))
			.translate(-rotOffset.x, -rotOffset.y, -rotOffset.z);
	}
}
