package com.simibubi.create.modules.contraptions.base;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
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
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class KineticTileEntityRenderer extends TileEntityRendererFast<KineticTileEntity> {

	protected static Map<BlockState, BufferManipulator> cachedBuffers;

	protected static class BlockModelSpinner extends BufferManipulator {

		public BlockModelSpinner(ByteBuffer original) {
			super(original);
		}

		public ByteBuffer getTransformed(float xIn, float yIn, float zIn, float angle, Axis axis,
				int packedLightCoords) {
			original.rewind();
			mutable.rewind();
			float cos = MathHelper.cos(angle);
			float sin = MathHelper.sin(angle);
			float x, y, z = 0;

			for (int vertex = 0; vertex < vertexCount(original); vertex++) {
				x = getX(original, vertex) - .5f;
				y = getY(original, vertex) - .5f;
				z = getZ(original, vertex) - .5f;

				putPos(mutable, vertex, rotateX(x, y, z, sin, cos, axis) + .5f + xIn,
						rotateY(x, y, z, sin, cos, axis) + .5f + yIn, rotateZ(x, y, z, sin, cos, axis) + .5f + zIn);
				putLight(mutable, vertex, packedLightCoords);
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
		cacheIfMissing(state, getWorld(), BlockModelSpinner::new);

		final BlockPos pos = te.getPos();
		Axis axis = ((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState());
		float time = AnimationTickHolder.getRenderTick();
		float offset = getRotationOffsetForPosition(te, pos, axis);
		float angle = (float) (((time * te.getSpeed() + offset) % 360) / 180 * (float) Math.PI);

		renderFromCache(buffer, state, getWorld(), (float) x, (float) y, (float) z, pos, axis, angle);
	}

	protected static void renderFromCache(BufferBuilder buffer, BlockState state, World world, float x, float y,
			float z, BlockPos pos, Axis axis, float angle) {
		int packedLightmapCoords = state.getPackedLightmapCoords(world, pos);
		buffer.putBulkData(((BlockModelSpinner) getBuffer(state)).getTransformed(x, y, z, angle, axis,
				packedLightmapCoords));
	}

	public static BufferManipulator getBuffer(BlockState state) {
		return cachedBuffers.get(state);
	}

	public static void cacheIfMissing(final BlockState state, World world,
			Function<ByteBuffer, BufferManipulator> factory) {
		if (!cachedBuffers.containsKey(state)) {
			BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
			IBakedModel originalModel = dispatcher.getModelForState(state);
			BufferBuilder builder = new BufferBuilder(0);
			Random random = new Random();

			builder.setTranslation(0, 1, 0);
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			blockRenderer.renderModelFlat(world, originalModel, state, BlockPos.ZERO.down(), builder, true, random, 42,
					EmptyModelData.INSTANCE);
			builder.finishDrawing();

			cachedBuffers.put(state, factory.apply(builder.getByteBuffer()));
		}
	}

	protected float getRotationOffsetForPosition(KineticTileEntity te, final BlockPos pos, final Axis axis) {
		float offset = AllBlocks.LARGE_COGWHEEL.typeOf(te.getBlockState()) ? 11.25f : 0;
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

	public static void invalidateCache() {
		if (cachedBuffers != null)
			cachedBuffers.clear();
	}

}
