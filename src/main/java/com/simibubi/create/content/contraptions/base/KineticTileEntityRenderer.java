package com.simibubi.create.content.contraptions.base;

import org.apache.commons.lang3.ArrayUtils;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class KineticTileEntityRenderer extends SafeTileEntityRenderer<KineticTileEntity> {

	public static final SuperByteBufferCache.Compartment<BlockState> KINETIC_TILE = new SuperByteBufferCache.Compartment<>();
	public static boolean rainbowMode = false;

	protected static final RenderType[] REVERSED_CHUNK_BUFFER_LAYERS = RenderType.chunkBufferLayers().toArray(RenderType[]::new);
	static {
		ArrayUtils.reverse(REVERSED_CHUNK_BUFFER_LAYERS);
	}

	public KineticTileEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (Backend.canUseInstancing(te.getLevel())) return;

		BlockState state = getRenderedBlockState(te);
		RenderType type = getRenderType(te, state);
		if (type != null)
			renderRotatingBuffer(te, getRotatedModel(te, state), ms, buffer.getBuffer(type), light);
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return te.getBlockState();
	}

	protected RenderType getRenderType(KineticTileEntity te, BlockState state) {
		for (RenderType type : REVERSED_CHUNK_BUFFER_LAYERS)
			if (ItemBlockRenderTypes.canRenderInLayer(state, type))
				return type;
		return null;
	}

	protected SuperByteBuffer getRotatedModel(KineticTileEntity te, BlockState state) {
		return CachedBufferer.block(KINETIC_TILE, state);
	}

	public static void renderRotatingKineticBlock(KineticTileEntity te, BlockState renderedState, PoseStack ms,
		VertexConsumer buffer, int light) {
		SuperByteBuffer superByteBuffer = CachedBufferer.block(KINETIC_TILE, renderedState);
		renderRotatingBuffer(te, superByteBuffer, ms, buffer, light);
	}

	public static void renderRotatingBuffer(KineticTileEntity te, SuperByteBuffer superBuffer, PoseStack ms,
		VertexConsumer buffer, int light) {
		standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, buffer);
	}

	public static float getAngleForTe(KineticTileEntity te, final BlockPos pos, Axis axis) {
		float time = AnimationTickHolder.getRenderTime(te.getLevel());
		float offset = getRotationOffsetForPosition(te, pos, axis);
		float angle = ((time * te.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
		return angle;
	}

	public static SuperByteBuffer standardKineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te,
		int light) {
		final BlockPos pos = te.getBlockPos();
		Axis axis = ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
		return kineticRotationTransform(buffer, te, axis, getAngleForTe(te, pos, axis), light);
	}

	public static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te, Axis axis,
		float angle, int light) {
		buffer.light(light);
		buffer.rotateCentered(Direction.get(AxisDirection.POSITIVE, axis), angle);

		if (KineticDebugger.isActive()) {
			rainbowMode = true;
			buffer.color(te.hasNetwork() ? Color.generateFromLong(te.network) : Color.WHITE);
		} else {
			float overStressedEffect = te.effects.overStressedEffect;
			if (overStressedEffect != 0)
				if (overStressedEffect > 0)
					buffer.color(Color.WHITE.mixWith(Color.RED, overStressedEffect));
				else
					buffer.color(Color.WHITE.mixWith(Color.SPRING_GREEN, -overStressedEffect));
			else
				buffer.color(Color.WHITE);
		}

		return buffer;
	}

	public static float getRotationOffsetForPosition(KineticTileEntity te, final BlockPos pos, final Axis axis) {
		float offset = ICogWheel.isLargeCog(te.getBlockState()) ? 11.25f : 0;
		double d = (((axis == Axis.X) ? 0 : pos.getX()) + ((axis == Axis.Y) ? 0 : pos.getY())
			+ ((axis == Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0)
			offset = 22.5f;
		return offset + te.getRotationAngleOffset(axis);
	}

	public static BlockState shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.setValue(BlockStateProperties.AXIS, axis);
	}

	public static Axis getRotationAxisOf(KineticTileEntity te) {
		return ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
	}

}
