package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionEntity;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

public class PulleyRenderer extends KineticTileEntityRenderer {

	public PulleyRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		PulleyTileEntity pulley = (PulleyTileEntity) te;
		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();

		SuperByteBuffer halfMagnet = AllBlockPartials.ROPE_HALF_MAGNET.renderOn(blockState);
		SuperByteBuffer halfRope = AllBlockPartials.ROPE_HALF.renderOn(blockState);
		SuperByteBuffer magnet = CreateClient.bufferCache.renderBlock(AllBlocks.PULLEY_MAGNET.getDefaultState());
		SuperByteBuffer rope = CreateClient.bufferCache.renderBlock(AllBlocks.ROPE.getDefaultState());

		boolean moving = pulley.running && (pulley.movedContraption == null || !pulley.movedContraption.isStalled());
		float offset = pulley.getInterpolatedOffset(moving ? partialTicks : 0.5f);
		
		if (pulley.movedContraption != null) {
			ContraptionEntity e = pulley.movedContraption;
			PulleyContraption c = (PulleyContraption) pulley.movedContraption.getContraption();
			double entityPos = MathHelper.lerp(partialTicks, e.lastTickPosY, e.getY());
			offset = (float) -(entityPos - c.getAnchor().getY() - c.initialOffset);
		}
		
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		
		if (pulley.running || pulley.offset == 0)
			renderAt(te.getWorld(), offset > .25f ? magnet : halfMagnet, offset, pos, ms, vb);

		float f = offset % 1;
		if (offset > .75f && (f < .25f || f > .75f))
			renderAt(te.getWorld(), halfRope, f > .75f ? f - 1 : f, pos, ms, vb);

		if (!pulley.running)
			return;

		for (int i = 0; i < offset - 1.25f; i++)
			renderAt(te.getWorld(), rope, offset - i - 1, pos, ms, vb);
	}

	public void renderAt(IWorld world, SuperByteBuffer partial, float offset, BlockPos pulleyPos,
			MatrixStack ms, IVertexBuilder buffer) {
		BlockPos actualPos = pulleyPos.down((int) offset);
		int light = WorldRenderer.getLightmapCoordinates(world, world.getBlockState(actualPos), actualPos);
		partial.translate(0, -offset, 0).light(light).renderInto(ms, buffer);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		BlockState blockState = te.getBlockState();
		return AllBlockPartials.ROPE_COIL.renderOnDirectional(blockState, horizontalFacing(blockState));
	}

	public Direction horizontalFacing(BlockState blockState) {
		return Direction.getFacingFromAxis(AxisDirection.POSITIVE, blockState.get(PulleyBlock.HORIZONTAL_AXIS));
	}

}
