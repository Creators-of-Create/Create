package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class AbstractPulleyRenderer extends KineticTileEntityRenderer {

	private AllBlockPartials halfRope;
	private AllBlockPartials halfMagnet;

	public AbstractPulleyRenderer(TileEntityRendererDispatcher dispatcher, AllBlockPartials halfRope,
		AllBlockPartials halfMagnet) {
		super(dispatcher);
		this.halfRope = halfRope;
		this.halfMagnet = halfMagnet;
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		float offset = getOffset(te, partialTicks);
		boolean running = isRunning(te);

		Axis rotationAxis = ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
		kineticRotationTransform(getRotatedCoil(te), te, rotationAxis, AngleHelper.rad(offset * 180), light)
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));

		World world = te.getWorld();
		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();

		SuperByteBuffer halfMagnet = this.halfMagnet.renderOn(blockState);
		SuperByteBuffer halfRope = this.halfRope.renderOn(blockState);
		SuperByteBuffer magnet = renderMagnet(te);
		SuperByteBuffer rope = renderRope(te);

		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		if (running || offset == 0)
			renderAt(world, offset > .25f ? magnet : halfMagnet, offset, pos, ms, vb);

		float f = offset % 1;
		if (offset > .75f && (f < .25f || f > .75f))
			renderAt(world, halfRope, f > .75f ? f - 1 : f, pos, ms, vb);

		if (!running)
			return;

		for (int i = 0; i < offset - 1.25f; i++)
			renderAt(world, rope, offset - i - 1, pos, ms, vb);
	}

	private void renderAt(IWorld world, SuperByteBuffer partial, float offset, BlockPos pulleyPos, MatrixStack ms,
		IVertexBuilder buffer) {
		BlockPos actualPos = pulleyPos.down((int) offset);
		int light = WorldRenderer.getLightmapCoordinates(world, world.getBlockState(actualPos), actualPos);
		partial.translate(0, -offset, 0)
			.light(light)
			.renderInto(ms, buffer);
	}

	protected abstract Axis getShaftAxis(KineticTileEntity te);

	protected abstract AllBlockPartials getCoil();

	protected abstract SuperByteBuffer renderRope(KineticTileEntity te);

	protected abstract SuperByteBuffer renderMagnet(KineticTileEntity te);

	protected abstract float getOffset(KineticTileEntity te, float partialTicks);

	protected abstract boolean isRunning(KineticTileEntity te);

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getShaftAxis(te));
	}

	protected SuperByteBuffer getRotatedCoil(KineticTileEntity te) {
		BlockState blockState = te.getBlockState();
		return getCoil().renderOnDirectionalSouth(blockState,
			Direction.getFacingFromAxis(AxisDirection.POSITIVE, getShaftAxis(te)));
	}

}
