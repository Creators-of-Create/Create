package com.simibubi.create.foundation.render;

import java.util.SortedMap;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.Util;

public class SuperRenderTypeBuffer implements IRenderTypeBuffer {

	static SuperRenderTypeBuffer instance;

	public static SuperRenderTypeBuffer getInstance() {
		if (instance == null)
			instance = new SuperRenderTypeBuffer();
		return instance;
	}

	SuperRenderTypeBufferPhase earlyBuffer;
	SuperRenderTypeBufferPhase defaultBuffer;
	SuperRenderTypeBufferPhase lateBuffer;

	public SuperRenderTypeBuffer() {
		earlyBuffer = new SuperRenderTypeBufferPhase();
		defaultBuffer = new SuperRenderTypeBufferPhase();
		lateBuffer = new SuperRenderTypeBufferPhase();
	}

	public IVertexBuilder getEarlyBuffer(RenderType type) {
		return earlyBuffer.getBuffer(type);
	}

	@Override
	public IVertexBuilder getBuffer(RenderType type) {
		return defaultBuffer.getBuffer(type);
	}

	public IVertexBuilder getLateBuffer(RenderType type) {
		return lateBuffer.getBuffer(type);
	}

	public void draw() {
		earlyBuffer.endBatch();
		defaultBuffer.endBatch();
		lateBuffer.endBatch();
	}

	public void draw(RenderType type) {
		earlyBuffer.endBatch(type);
		defaultBuffer.endBatch(type);
		lateBuffer.endBatch(type);
	}

	private static class SuperRenderTypeBufferPhase extends IRenderTypeBuffer.Impl {

		// Visible clones from net.minecraft.client.renderer.RenderTypeBuffers
		static final RegionRenderCacheBuilder blockBuilders = new RegionRenderCacheBuilder();

		static final SortedMap<RenderType, BufferBuilder> createEntityBuilders() {
			return Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
				map.put(Atlases.solidBlockSheet(), blockBuilders.builder(RenderType.solid()));
				assign(map, RenderTypes.getOutlineSolid());
				map.put(Atlases.cutoutBlockSheet(), blockBuilders.builder(RenderType.cutout()));
				map.put(Atlases.bannerSheet(), blockBuilders.builder(RenderType.cutoutMipped()));
				map.put(Atlases.translucentCullBlockSheet(), blockBuilders.builder(RenderType.translucent())); // FIXME new equivalent of getEntityTranslucent() ?
				assign(map, Atlases.shieldSheet());
				assign(map, Atlases.bedSheet());
				assign(map, Atlases.shulkerBoxSheet());
				assign(map, Atlases.signSheet());
				assign(map, Atlases.chestSheet());
				assign(map, RenderType.translucentNoCrumbling());
				assign(map, RenderType.glint());
				assign(map, RenderType.entityGlint());
				assign(map, RenderType.waterMask());
				ModelBakery.DESTROY_TYPES.forEach((p_228488_1_) -> {
					assign(map, p_228488_1_);
				});
			});
		}

		private static void assign(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> map, RenderType type) {
			map.put(type, new BufferBuilder(type.bufferSize()));
		}

		protected SuperRenderTypeBufferPhase() {
			super(new BufferBuilder(256), createEntityBuilders());
		}

	}

}
