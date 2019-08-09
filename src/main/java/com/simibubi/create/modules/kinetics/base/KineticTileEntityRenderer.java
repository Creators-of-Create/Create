package com.simibubi.create.modules.kinetics.base;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BufferManipulator;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class KineticTileEntityRenderer extends TileEntityRendererFast<KineticTileEntity> {

	protected static Map<BlockState, BufferManipulator> cachedBuffers;

	protected class BlockModelSpinner extends BufferManipulator {

		public BlockModelSpinner(ByteBuffer original) {
			super(original);
		}

		public ByteBuffer getTransformed(Vec3d translation, float angle, Axis axis, int packedLightCoords) {
			original.rewind();
			mutable.rewind();
			final float cos = MathHelper.cos(angle);
			final float sin = MathHelper.sin(angle);
			final Vec3d half = new Vec3d(.5f, .5f, .5f);

			forEachVertex(original, index -> {
				final Vec3d vertex = getPos(original, index).subtract(half);
				putPos(mutable, index, rotatePos(vertex, sin, cos, axis).add(translation).add(half));
				mutable.putInt(index + 24, packedLightCoords);
			});

			return mutable;
		}
	}

	public KineticTileEntityRenderer() {
		cachedBuffers = new HashMap<>();
	}

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {

		final BlockState state = getRenderedBlockState(te);
		cacheIfMissing(state, BlockModelSpinner::new);

		final Vec3d translation = new Vec3d(x, y, z);
		final BlockPos pos = te.getPos();
		final Axis axis = ((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState());
		float time = Animation.getWorldTime(Minecraft.getInstance().world, partialTicks);
		float offset = getRotationOffsetForPosition(te, pos, axis);
		float angle = (float) (((time * te.getSpeed() + offset) % 360) / 180 * (float) Math.PI);

		renderFromCache(buffer, state, translation, pos, axis, angle);
	}

	protected void renderFromCache(BufferBuilder buffer, final BlockState state, final Vec3d translation,
			final BlockPos pos, final Axis axis, float angle) {
		int packedLightmapCoords = state.getPackedLightmapCoords(getWorld(), pos);
		buffer.putBulkData(((BlockModelSpinner) cachedBuffers.get(state)).getTransformed(translation, angle, axis,
				packedLightmapCoords));
	}

	protected void cacheIfMissing(final BlockState state, Function<ByteBuffer, BufferManipulator> factory) {
		if (!cachedBuffers.containsKey(state)) {
			BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
			IBakedModel originalModel = dispatcher.getModelForState(state);
			BufferBuilder builder = new BufferBuilder(0);
			Random random = new Random();

			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			blockRenderer.renderModelFlat(getWorld(), originalModel, state, BlockPos.ZERO, builder, true, random, 42,
					EmptyModelData.INSTANCE);
			builder.finishDrawing();

			cachedBuffers.put(state, factory.apply(builder.getByteBuffer()));
		}
	}

	protected float getRotationOffsetForPosition(KineticTileEntity te, final BlockPos pos, final Axis axis) {
		float offset = AllBlocks.LARGE_GEAR.typeOf(te.getBlockState()) ? 11.25f : 0;
		double d = (((axis == Axis.X) ? 0 : pos.getX()) + ((axis == Axis.Y) ? 0 : pos.getY())
				+ ((axis == Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0) {
			offset = 22.5f;
		}
		return offset;
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return te.getBlockState();
	}
	
}
