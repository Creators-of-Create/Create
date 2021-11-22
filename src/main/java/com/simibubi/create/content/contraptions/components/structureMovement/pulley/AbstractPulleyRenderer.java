package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractPulleyRenderer extends KineticTileEntityRenderer {

	private PartialModel halfRope;
	private PartialModel halfMagnet;

	public AbstractPulleyRenderer(BlockEntityRendererProvider.Context context, PartialModel halfRope,
		PartialModel halfMagnet) {
		super(context);
		this.halfRope = halfRope;
		this.halfMagnet = halfMagnet;
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.getInstance()
			.canUseInstancing(te.getLevel()))
			return;

		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		float offset = getOffset(te, partialTicks);
		boolean running = isRunning(te);

		Axis rotationAxis = ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
		kineticRotationTransform(getRotatedCoil(te), te, rotationAxis, AngleHelper.rad(offset * 180), light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		Level world = te.getLevel();
		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getBlockPos();

		SuperByteBuffer halfMagnet = CachedBufferer.partial(this.halfMagnet, blockState);
		SuperByteBuffer halfRope = CachedBufferer.partial(this.halfRope, blockState);
		SuperByteBuffer magnet = renderMagnet(te);
		SuperByteBuffer rope = renderRope(te);

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
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

	private void renderAt(LevelAccessor world, SuperByteBuffer partial, float offset, BlockPos pulleyPos, PoseStack ms,
		VertexConsumer buffer) {
		BlockPos actualPos = pulleyPos.below((int) offset);
		int light = LevelRenderer.getLightColor(world, world.getBlockState(actualPos), actualPos);
		partial.translate(0, -offset, 0)
			.light(light)
			.renderInto(ms, buffer);
	}

	protected abstract Axis getShaftAxis(KineticTileEntity te);

	protected abstract PartialModel getCoil();

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
		return CachedBufferer.partialFacing(getCoil(), blockState,
			Direction.get(AxisDirection.POSITIVE, getShaftAxis(te)));
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

}
