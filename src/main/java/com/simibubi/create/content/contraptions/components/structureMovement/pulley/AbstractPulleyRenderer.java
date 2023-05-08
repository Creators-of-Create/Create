package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
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

public abstract class AbstractPulleyRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {

	private PartialModel halfRope;
	private PartialModel halfMagnet;

	public AbstractPulleyRenderer(BlockEntityRendererProvider.Context context, PartialModel halfRope,
		PartialModel halfMagnet) {
		super(context);
		this.halfRope = halfRope;
		this.halfMagnet = halfMagnet;
	}

	@Override
	public boolean shouldRenderOffScreen(T p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.canUseInstancing(be.getLevel()))
			return;

		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		float offset = getOffset(be, partialTicks);
		boolean running = isRunning(be);

		Axis rotationAxis = ((IRotate) be.getBlockState()
			.getBlock()).getRotationAxis(be.getBlockState());
		kineticRotationTransform(getRotatedCoil(be), be, rotationAxis, AngleHelper.rad(offset * 180), light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		Level world = be.getLevel();
		BlockState blockState = be.getBlockState();
		BlockPos pos = be.getBlockPos();

		SuperByteBuffer halfMagnet = CachedBufferer.partial(this.halfMagnet, blockState);
		SuperByteBuffer halfRope = CachedBufferer.partial(this.halfRope, blockState);
		SuperByteBuffer magnet = renderMagnet(be);
		SuperByteBuffer rope = renderRope(be);

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

	public static void renderAt(LevelAccessor world, SuperByteBuffer partial, float offset, BlockPos pulleyPos,
		PoseStack ms, VertexConsumer buffer) {
		BlockPos actualPos = pulleyPos.below((int) offset);
		int light = LevelRenderer.getLightColor(world, world.getBlockState(actualPos), actualPos);
		partial.translate(0, -offset, 0)
			.light(light)
			.renderInto(ms, buffer);
	}

	protected abstract Axis getShaftAxis(T be);

	protected abstract PartialModel getCoil();

	protected abstract SuperByteBuffer renderRope(T be);

	protected abstract SuperByteBuffer renderMagnet(T be);

	protected abstract float getOffset(T be, float partialTicks);

	protected abstract boolean isRunning(T be);

	@Override
	protected BlockState getRenderedBlockState(T be) {
		return shaft(getShaftAxis(be));
	}

	protected SuperByteBuffer getRotatedCoil(T be) {
		BlockState blockState = be.getBlockState();
		return CachedBufferer.partialFacing(getCoil(), blockState,
			Direction.get(AxisDirection.POSITIVE, getShaftAxis(be)));
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

}
