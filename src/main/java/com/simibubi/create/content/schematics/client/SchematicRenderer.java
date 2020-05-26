package com.simibubi.create.content.schematics.client;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.schematics.SchematicWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicRenderer {

	private final RegionRenderCacheBuilder bufferCache = new RegionRenderCacheBuilder();
	private final Set<RenderType> usedBlockRenderLayers = new HashSet<>(RenderType.getBlockLayers()
		.size());
	private final Set<RenderType> startedBufferBuilders = new HashSet<>(RenderType.getBlockLayers()
		.size());
	private boolean active;
	private boolean changed;
	private SchematicWorld schematic;
	private BlockPos anchor;

	public SchematicRenderer() {
		changed = false;
	}

	public void startHologram(SchematicWorld world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.active = true;
		this.changed = true;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void update() {
		changed = true;
	}

	public void tick() {
		if (!active)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.world == null || mc.player == null || !changed)
			return;

		redraw(mc);
		changed = false;
	}

	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		// TODO 1.15 buffered render
//		if (!active)
//			return;
//
//		final Entity entity = Minecraft.getInstance()
//			.getRenderViewEntity();
//
//		if (entity == null) {
//			return;
//		}
//
//		ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
//		Vec3d view = renderInfo.getProjectedView();
//		double renderPosX = view.x;
//		double renderPosY = view.y;
//		double renderPosZ = view.z;
//
//		RenderSystem.enableAlphaTest();
//		RenderSystem.enableBlend();
//		Minecraft.getInstance()
//			.getTextureManager()
//			.bindTexture(PlayerContainer.BLOCK_ATLAS_TEXTURE);
//
//		for (RenderType layer : RenderType.getBlockLayers()) {
//			if (!usedBlockRenderLayers.contains(layer)) {
//				continue;
//			}
//			final BufferBuilder bufferBuilder = bufferCache.get(layer);
//			RenderSystem.pushMatrix();
//			RenderSystem.translated(-renderPosX, -renderPosY, -renderPosZ);
//			drawBuffer(bufferBuilder);
//			RenderSystem.popMatrix();
//		}
//		RenderSystem.disableAlphaTest();
//		RenderSystem.disableBlend();
	}

	private void redraw(Minecraft minecraft) {
		usedBlockRenderLayers.clear();
		startedBufferBuilders.clear();

		final SchematicWorld blockAccess = schematic;
		final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

		List<BlockState> blockstates = new LinkedList<>();

		BlockPos.func_229383_a_(blockAccess.getBounds())
			.forEach(localPos -> {
				BlockPos pos = localPos.add(anchor);
				BlockState state = blockAccess.getBlockState(pos);
				
				for (RenderType blockRenderLayer : RenderType.getBlockLayers()) {
					if (!RenderTypeLookup.canRenderInLayer(state, blockRenderLayer))
						continue;
					ForgeHooksClient.setRenderLayer(blockRenderLayer);
					
					final BufferBuilder bufferBuilder = bufferCache.get(blockRenderLayer);
					if (startedBufferBuilders.add(blockRenderLayer))
						bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					if (blockRendererDispatcher.renderModel(state, pos, blockAccess, new MatrixStack(), bufferBuilder,
						true, minecraft.world.rand, EmptyModelData.INSTANCE)) {
						usedBlockRenderLayers.add(blockRenderLayer);
					}
					blockstates.add(state);
				}
				
				ForgeHooksClient.setRenderLayer(null);
			});

		// finishDrawing
		for (RenderType layer : RenderType.getBlockLayers()) {
			if (!startedBufferBuilders.contains(layer)) {
				continue;
			}
			bufferCache.get(layer)
				.finishDrawing();
		}
	}

//	private static void drawBuffer(final BufferBuilder bufferBuilder) {
//		Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popData();
//		BufferBuilder.DrawState state = pair.getFirst();
//
//		if (state.getCount() > 0) {
//			state.getVertexFormat()
//				.startDrawing(MemoryUtil.memAddress(pair.getSecond()));
//			RenderSystem.drawArrays(state.getMode(), 0, state.getCount());
//			state.getVertexFormat()
//				.endDrawing();
//		}
//	}

}
