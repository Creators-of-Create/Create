package com.simibubi.create.modules.logistics.block.diodes;

import java.nio.ByteBuffer;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.BufferManipulator;
import com.simibubi.create.foundation.utility.ColorHelper;

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

public class FlexpeaterTileEntityRenderer extends TileEntityRendererFast<FlexpeaterTileEntity> {

	protected class FlexpeaterIndicatorRenderer extends BufferManipulator {

		public FlexpeaterIndicatorRenderer(ByteBuffer original) {
			super(original);
		}

		public ByteBuffer getTransformed(float xIn, float yIn, float zIn, float colorModifier, int packedLightCoords) {
			original.rewind();
			mutable.rewind();

			int color = ColorHelper.mixColors(0x2C0300, 0xCD0000, colorModifier);

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

	protected static FlexpeaterIndicatorRenderer cachedIndicator;

	@Override
	public void renderTileEntityFast(FlexpeaterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockPos pos = te.getPos();

		if (cachedIndicator == null) {
			BlockState renderedState = AllBlocks.FLEXPEATER_INDICATOR.get().getDefaultState();
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

			cachedIndicator = new FlexpeaterIndicatorRenderer(builder.getByteBuffer());
		}

		BlockState blockState = te.getBlockState();
		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);
		buffer.putBulkData(cachedIndicator.getTransformed((float) x, (float) y, (float) z,
				te.state / (float) te.maxState, packedLightmapCoords));
	}

	public static void invalidateCache() {
		cachedIndicator = null;
	}

}
