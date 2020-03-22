package com.simibubi.create.modules.schematics.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.foundation.type.Cuboid;
import com.simibubi.create.modules.schematics.SchematicWorld;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicHologram {

	private final RegionRenderCacheBuilder bufferCache = new RegionRenderCacheBuilder();
	private final Set<RenderType> usedBlockRenderLayers = new HashSet<>(RenderType.getBlockLayers().size());
	private final Set<RenderType> startedBufferBuilders = new HashSet<>(RenderType.getBlockLayers().size());
	private boolean active;
	private boolean changed;
	private SchematicWorld schematic;
	private BlockPos anchor;

	public SchematicHologram() {
		changed = false;
	}

	public void startHologram(Template schematic, BlockPos anchor) {
		SchematicWorld world = new SchematicWorld(new HashMap<>(), new Cuboid(BlockPos.ZERO, BlockPos.ZERO), anchor,
				Minecraft.getInstance().world);
		schematic.addBlocksToWorld(world, anchor, new PlacementSettings());
		startHologram(world);
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
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.world == null)
			return;
		if (minecraft.player == null)
			return;
		if (changed) {
			redraw(minecraft);
			changed = false;
		}
	}

	private void redraw(Minecraft minecraft) {
		usedBlockRenderLayers.clear();
		startedBufferBuilders.clear();

		final SchematicWorld blockAccess = schematic;
		final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

		List<BlockState> blockstates = new LinkedList<>();

		for (BlockPos localPos : BlockPos.getAllInBoxMutable(blockAccess.getBounds().getOrigin(),
				blockAccess.getBounds().getOrigin().add(blockAccess.getBounds().getSize()))) {
			BlockPos pos = localPos.add(anchor);
			BlockState state = blockAccess.getBlockState(pos);
			for (RenderType blockRenderLayer : RenderType.getBlockLayers()) {
				if (!RenderTypeLookup.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				ForgeHooksClient.setRenderLayer(blockRenderLayer);
				final BufferBuilder bufferBuilder = bufferCache.get(blockRenderLayer);
				if (startedBufferBuilders.add(blockRenderLayer)) {
					// Copied from RenderChunk
					{
						bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					}
				}
				// OptiFine Shaders compatibility
				// if (Config.isShaders()) SVertexBuilder.pushEntity(state, pos,
				// blockAccess, bufferBuilder);

				// Block transformations
				if (state.getBlock() instanceof BedBlock) {
					state = Blocks.QUARTZ_SLAB.getDefaultState();
				}

				if (blockRendererDispatcher.renderModel(state, pos, blockAccess, new MatrixStack(), bufferBuilder, true, minecraft.world.rand, EmptyModelData.INSTANCE)) {
					usedBlockRenderLayers.add(blockRenderLayer);
				}
				blockstates.add(state);
				// if (Config.isShaders())
				// SVertexBuilder.popEntity(bufferBuilder);
			}
			ForgeHooksClient.setRenderLayer(null);
		}

		// finishDrawing
		for (RenderType layer : RenderType.getBlockLayers()) {
			if (!startedBufferBuilders.contains(layer)) {
				continue;
			}
			bufferCache.get(layer).finishDrawing();
		}
	}

	public void render() {
		if (active) {
			final Entity entity = Minecraft.getInstance().getRenderViewEntity();

			if (entity == null) {
				return;
			}

			ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
			Vec3d view = renderInfo.getProjectedView();
			double renderPosX = view.x;
			double renderPosY = view.y;
			double renderPosZ = view.z;

			RenderSystem.enableAlphaTest();
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

			for (RenderType layer : RenderType.getBlockLayers()) {
				if (!usedBlockRenderLayers.contains(layer)) {
					continue;
				}
				final BufferBuilder bufferBuilder = bufferCache.get(layer);
				RenderSystem.pushMatrix();
				RenderSystem.translated(-renderPosX, -renderPosY, -renderPosZ);
				drawBuffer(bufferBuilder);
				RenderSystem.popMatrix();
			}
			RenderSystem.disableAlphaTest();
			RenderSystem.disableBlend();
		}
	}

	// Coppied from WorldVertexBufferUploader - Draw everything but don't
	// reset the buffer
	private static void drawBuffer(final BufferBuilder bufferBuilder) {
		Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popData();
		BufferBuilder.DrawState state = pair.getFirst();

		if (state.getCount() > 0) {
			state.getVertexFormat().startDrawing(MemoryUtil.memAddress(pair.getSecond()));
			GlStateManager.drawArrays(state.getMode(), 0, state.getCount());
			state.getVertexFormat().endDrawing();
		}
	}
}
