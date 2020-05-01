package com.simibubi.create.modules.schematics.client;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;
import com.simibubi.create.foundation.utility.render.StructureRenderer;
import com.simibubi.create.modules.schematics.SchematicWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Usage;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicRenderer {

	private final RegionRenderCacheBuilder bufferCache = new RegionRenderCacheBuilder();
	private final boolean[] usedBlockRenderLayers = new boolean[BlockRenderLayer.values().length];
	private final boolean[] startedBufferBuilders = new boolean[BlockRenderLayer.values().length];
	private boolean active;
	private boolean changed;
	private SchematicWorld schematic;
	private AABBOutline outline;
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

	public void render() {
		if (!active)
			return;

		GlStateManager.disableCull();
		GlStateManager.enableAlphaTest();
		GlStateManager.depthMask(true);
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++)
			if (usedBlockRenderLayers[blockRenderLayerId])
				drawBuffer(bufferCache.getBuilder(blockRenderLayerId));

		GlStateManager.pushMatrix();
		Vec3d position = new Vec3d(anchor);
		Vec3d rotation = Vec3d.ZERO;
		StructureRenderer.renderTileEntities(schematic, position, rotation, schematic.getTileEntities());
		GlStateManager.popMatrix();
	}

	private void redraw(Minecraft minecraft) {
		Arrays.fill(usedBlockRenderLayers, false);
		Arrays.fill(startedBufferBuilders, false);

		SchematicWorld blockAccess = schematic;
		blockAccess.renderMode = true;
		BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();
		List<BlockState> blockstates = new LinkedList<>();
		BlockPos min = blockAccess.getBounds().getOrigin();
		BlockPos max = min.add(blockAccess.getBounds().getSize());
		outline = new AABBOutline(new AxisAlignedBB(min, max));
		outline.setTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED);

		for (BlockPos localPos : BlockPos.getAllInBoxMutable(min, max)) {
			BlockPos pos = localPos.add(anchor);
			BlockState state = blockAccess.getBlockState(pos);

			for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!state.getBlock().canRenderInLayer(state, blockRenderLayer))
					continue;
				ForgeHooksClient.setRenderLayer(blockRenderLayer);

				final int blockRenderLayerId = blockRenderLayer.ordinal();
				final BufferBuilder bufferBuilder = bufferCache.getBuilder(blockRenderLayerId);
				if (!startedBufferBuilders[blockRenderLayerId]) {
					startedBufferBuilders[blockRenderLayerId] = true;
					bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				}

				usedBlockRenderLayers[blockRenderLayerId] |= blockRendererDispatcher.renderBlock(state, pos,
						blockAccess, bufferBuilder, minecraft.world.rand, EmptyModelData.INSTANCE);
				blockstates.add(state);
			}
			ForgeHooksClient.setRenderLayer(null);
		}
		
		// finishDrawing
		blockAccess.renderMode = false;
		for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
			if (!startedBufferBuilders[blockRenderLayerId])
				continue;
			bufferCache.getBuilder(blockRenderLayerId).finishDrawing();
		}
	}

	// Coppied from the Tesselator's vboUploader - Draw everything but don't
	// reset the buffer
	private static void drawBuffer(final BufferBuilder bufferBuilder) {
		if (bufferBuilder.getVertexCount() <= 0)
			return;

		VertexFormat vertexformat = bufferBuilder.getVertexFormat();
		int size = vertexformat.getSize();
		ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
		List<VertexFormatElement> list = vertexformat.getElements();

		for (int index = 0; index < list.size(); ++index) {
			VertexFormatElement vertexformatelement = list.get(index);
			Usage usage = vertexformatelement.getUsage();
			bytebuffer.position(vertexformat.getOffset(index));
			usage.preDraw(vertexformat, index, size, bytebuffer);
		}

		GlStateManager.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());

		for (int index = 0; index < list.size(); ++index) {
			VertexFormatElement vertexformatelement = list.get(index);
			Usage usage = vertexformatelement.getUsage();
			usage.postDraw(vertexformat, index, size, bytebuffer);
		}
	}

}
