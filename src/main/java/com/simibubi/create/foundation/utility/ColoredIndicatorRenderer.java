package com.simibubi.create.foundation.utility;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ColoredIndicatorRenderer extends BufferManipulator {

	protected static Map<BlockState, ColoredIndicatorRenderer> cachedBuffers = new HashMap<>();

	public ColoredIndicatorRenderer(ByteBuffer original) {
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

	public static <R extends ColoredIndicatorRenderer> void cacheIfMissing(BlockState renderedState,
			Function<ByteBuffer, R> factory) {
		if (!cachedBuffers.containsKey(renderedState)) {
			BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
			IBakedModel originalModel = dispatcher.getModelForState(renderedState);
			BufferBuilder builder = new BufferBuilder(0);
			Random random = new Random();

			builder.setTranslation(0, 1, 0);
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			blockRenderer.renderModelFlat(Minecraft.getInstance().world, originalModel, renderedState,
					BlockPos.ZERO.down(), builder, true, random, 42, EmptyModelData.INSTANCE);
			builder.finishDrawing();

			cachedBuffers.put(renderedState, factory.apply(builder.getByteBuffer()));
		}
	}
	
	public static ColoredIndicatorRenderer get(BlockState state) {
		cacheIfMissing(state, ColoredIndicatorRenderer::new);
		return cachedBuffers.get(state);
	}
	

	public static void invalidateCache() {
		cachedBuffers.clear();
	}

}
