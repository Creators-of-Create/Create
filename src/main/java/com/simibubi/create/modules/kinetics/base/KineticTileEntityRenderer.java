package com.simibubi.create.modules.kinetics.base;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;
import net.minecraftforge.client.model.data.EmptyModelData;

public class KineticTileEntityRenderer extends TileEntityRendererFast<KineticTileEntity> {

	protected static Map<BlockState, CachedByteBuffer> cachedBuffers;

	protected class CachedByteBuffer {
		ByteBuffer original;
		ByteBuffer mutable;

		public CachedByteBuffer(ByteBuffer original) {
			original.rewind();
			this.original = original;

			this.mutable = GLAllocation.createDirectByteBuffer(original.capacity());
			this.mutable.order(original.order());
			this.mutable.limit(original.limit());
			mutable.put(this.original);
			mutable.rewind();
		}

		public ByteBuffer getTransformed(Vec3d translation, float angle, Axis axis, int packedLightCoords) {
			original.rewind();
			mutable.rewind();
			final float cos = MathHelper.cos(angle);
			final float sin = MathHelper.sin(angle);
			final int formatLength = DefaultVertexFormats.BLOCK.getSize();

			for (int i = 0; i < original.limit() / formatLength; i++) {
				final int position = i * formatLength;
				final float x = original.getFloat(position) - .5f;
				final float y = original.getFloat(position + 4) - .5f;
				final float z = original.getFloat(position + 8) - .5f;

				float xr = x;
				float yr = y;
				float zr = z;

				if (axis == Axis.X) {
					yr = y * cos - z * sin;
					zr = z * cos + y * sin;
				}

				if (axis == Axis.Y) {
					xr = x * cos + z * sin;
					zr = z * cos - x * sin;
				}

				if (axis == Axis.Z) {
					yr = y * cos + x * sin;
					xr = x * cos - y * sin;
				}

				mutable.putFloat(position, (float) (xr + translation.x + .5f));
				mutable.putFloat(position + 4, (float) (yr + translation.y + .5f));
				mutable.putFloat(position + 8, (float) (zr + translation.z + .5f));
				mutable.putInt(position + 24, packedLightCoords);
			}
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
		cacheIfMissing(state);

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
		buffer.putBulkData(cachedBuffers.get(state).getTransformed(translation, angle, axis, packedLightmapCoords));
	}

	protected void cacheIfMissing(final BlockState state) {
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

			cachedBuffers.put(state, new CachedByteBuffer(builder.getByteBuffer()));
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
