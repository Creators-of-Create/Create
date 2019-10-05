package com.simibubi.create.modules.logistics.management.base;

import static com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.TYPE;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;
import net.minecraftforge.client.model.data.EmptyModelData;

public class LogisticalControllerTileEntityRenderer extends TileEntityRendererFast<LogisticalControllerTileEntity> {

	protected class LogisticalControllerIndicatorRenderer extends BufferManipulator {

		public LogisticalControllerIndicatorRenderer(ByteBuffer original) {
			super(original);
		}

		public ByteBuffer getTransformed(float xIn, float yIn, float zIn, int color, int packedLightCoords) {
			original.rewind();
			mutable.rewind();

			byte r = (byte) (color >> 16);
			byte g = (byte) ((color >> 8) & 0xFF);
			byte b = (byte) (color & 0xFF);
			byte a = (byte) 255;

			for (int vertex = 0; vertex < vertexCount(original); vertex++) {
				putColor(mutable, vertex, r, g, b, a);
				putPos(mutable, vertex, getX(original, vertex) + xIn, getY(original, vertex) + yIn,
						getZ(original, vertex) + zIn);
				putLight(mutable, vertex, packedLightCoords);
			}

			return mutable;
		}
	}

	protected static Map<BlockState, LogisticalControllerIndicatorRenderer> cachedBuffers = new HashMap<>();

	@Override
	public void renderTileEntityFast(LogisticalControllerTileEntity te, double x, double y, double z,
			float partialTicks, int destroyStage, BufferBuilder buffer) {
		BlockPos pos = te.getPos();
		BlockState blockState = te.getBlockState();
		
		if (AllBlocks.LOGISTICAL_INDEX.typeOf(blockState))
			return;

		BlockState renderedState = AllBlocks.LOGISTICAL_CONTROLLER_INDICATOR.get().getDefaultState()
				.with(FACING, blockState.get(FACING)).with(TYPE, blockState.get(TYPE));

		if (!cachedBuffers.containsKey(renderedState)) {
			BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
			IBakedModel originalModel = dispatcher.getModelForState(renderedState);
			BufferBuilder builder = new BufferBuilder(0);
			Random random = new Random();

			builder.setTranslation(0, 1, 0);
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			blockRenderer.renderModelFlat(getWorld(), originalModel, renderedState, BlockPos.ZERO.down(), builder, true,
					random, 42, EmptyModelData.INSTANCE);
			builder.finishDrawing();

			cachedBuffers.put(renderedState, new LogisticalControllerIndicatorRenderer(builder.getByteBuffer()));
		}

		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);
		buffer.putBulkData(cachedBuffers.get(renderedState).getTransformed((float) x, (float) y, (float) z,
				te.getColor(), packedLightmapCoords));
	}

	public static void invalidateCache() {
		cachedBuffers.clear();
	}

}
