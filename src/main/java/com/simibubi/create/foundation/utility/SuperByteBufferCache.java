package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllBlockPartials;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SuperByteBufferCache {

	public static class Compartment<T> {
	}

	public static final Compartment<BlockState> GENERIC_TILE = new Compartment<>();
	public static final Compartment<AllBlockPartials> PARTIAL = new Compartment<>();
	
	Map<Compartment<?>, Cache<Object, SuperByteBuffer>> cache;

	public SuperByteBufferCache() {
		cache = new HashMap<>();
		registerCompartment(GENERIC_TILE);
		registerCompartment(PARTIAL);
	}

	public SuperByteBuffer renderBlock(BlockState toRender) {
		return getGeneric(toRender, () -> standardBlockRender(toRender));
	}
	
	public SuperByteBuffer renderPartial(AllBlockPartials partial, BlockState referenceState) {
		return get(PARTIAL, partial, () -> standardModelRender(partial.get(), referenceState));
	}

	public SuperByteBuffer renderBlockIn(Compartment<BlockState> compartment, BlockState toRender) {
		return get(compartment, toRender, () -> standardBlockRender(toRender));
	}

	 SuperByteBuffer getGeneric(BlockState key, Supplier<SuperByteBuffer> supplier) {
		return get(GENERIC_TILE, key, supplier);
	}

	public <T> SuperByteBuffer get(Compartment<T> compartment, T key, Supplier<SuperByteBuffer> supplier) {
		Cache<Object, SuperByteBuffer> compartmentCache = this.cache.get(compartment);
		try {
			return compartmentCache.get(key, supplier::get);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void registerCompartment(Compartment<?> instance) {
		cache.put(instance, CacheBuilder.newBuilder().build());
	}

	public void registerCompartment(Compartment<?> instance, long ticksTillExpired) {
		cache.put(instance,
				CacheBuilder.newBuilder().expireAfterAccess(ticksTillExpired * 50, TimeUnit.MILLISECONDS).build());
	}

	private SuperByteBuffer standardBlockRender(BlockState renderedState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		return standardModelRender(dispatcher.getModelForState(renderedState), renderedState);
	}
	
	private SuperByteBuffer standardModelRender(IBakedModel model, BlockState referenceState) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		BufferBuilder builder = new BufferBuilder(0);
		Random random = new Random();
		
		builder.setTranslation(0, 1, 0);
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		blockRenderer.renderModelFlat(Minecraft.getInstance().world, model, referenceState, BlockPos.ZERO.down(),
				builder, true, random, 42, EmptyModelData.INSTANCE);
		builder.finishDrawing();
		
		return new SuperByteBuffer(builder.getByteBuffer());
	}

	public void invalidate() {
		cache.forEach((comp, cache) -> {
			cache.invalidateAll();
		});
	}

}
