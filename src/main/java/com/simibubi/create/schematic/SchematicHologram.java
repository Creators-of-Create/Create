package com.simibubi.create.schematic;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.Usage;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public class SchematicHologram {

	// These buffers are large enough for an entire chunk, consider using
	// smaller buffers
	private static final RegionRenderCacheBuilder bufferCache = new RegionRenderCacheBuilder();
	private static final boolean[] usedBlockRenderLayers = new boolean[BlockRenderLayer.values().length];
	private static final boolean[] startedBufferBuilders = new boolean[BlockRenderLayer.values().length];

	private static SchematicHologram instance;
	private boolean active;
	private boolean changed;
	private SchematicWorld schematic;
	private BlockPos anchor;

	public SchematicHologram() {
		instance = this;
		changed = false;
	}

	public void startHologram(Template schematic, BlockPos anchor) {
		this.schematic = new SchematicWorld(new HashMap<>(), new Cuboid(BlockPos.ZERO, BlockPos.ZERO), anchor);
		this.anchor = anchor;
		schematic.addBlocksToWorld(this.schematic, anchor, new PlacementSettings());
		active = true;
		changed = true;
	}
	
	public void startHologram(SchematicWorld world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.active = true;
		this.changed = true;
	}

	public static SchematicHologram getInstance() {
		return instance;
	}

//	public static void display(Schematic schematic) {
//		instance = new SchematicHologram();
//		instance.startHologram(schematic);
//	}

	public static void reset() {
		instance = null;
	}

	public void schematicChanged() {
		changed = true;
	}

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {
		if (instance != null && instance.active) {
			final Minecraft minecraft = Minecraft.getInstance();
			if (event.phase != TickEvent.Phase.END)
				return;
			if (minecraft.world == null)
				return;
			if (minecraft.player == null)
				return;
			if (instance.changed) {
				redraw(minecraft);
				instance.changed = false;
			}
		}
	}

	private static void redraw(final Minecraft minecraft) {
		Arrays.fill(usedBlockRenderLayers, false);
		Arrays.fill(startedBufferBuilders, false);

		final SchematicWorld blockAccess = instance.schematic;
		final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

		List<BlockState> blockstates = new LinkedList<>();

		for (BlockPos localPos : BlockPos.getAllInBoxMutable(blockAccess.getBounds().getOrigin(),
				blockAccess.getBounds().getOrigin().add(blockAccess.getBounds().getSize()))) {
			BlockPos pos = localPos.add(instance.anchor);
			final BlockState state = blockAccess.getBlockState(pos);
			for (BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!state.getBlock().canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				ForgeHooksClient.setRenderLayer(blockRenderLayer);
				final int blockRenderLayerId = blockRenderLayer.ordinal();
				final BufferBuilder bufferBuilder = bufferCache.getBuilder(blockRenderLayerId);
				if (!startedBufferBuilders[blockRenderLayerId]) {
					startedBufferBuilders[blockRenderLayerId] = true;
					// Copied from RenderChunk
					{
						bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					}
				}
				// OptiFine Shaders compatibility
				// if (Config.isShaders()) SVertexBuilder.pushEntity(state, pos,
				// blockAccess, bufferBuilder);
				usedBlockRenderLayers[blockRenderLayerId] |= blockRendererDispatcher.renderBlock(state, pos,
						blockAccess, bufferBuilder, minecraft.world.rand, EmptyModelData.INSTANCE);
				blockstates.add(state);
				// if (Config.isShaders())
				// SVertexBuilder.popEntity(bufferBuilder);
			}
			ForgeHooksClient.setRenderLayer(null);
		}

		// finishDrawing
		for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
			if (!startedBufferBuilders[blockRenderLayerId]) {
				continue;
			}
			bufferCache.getBuilder(blockRenderLayerId).finishDrawing();
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		if (instance != null && instance.active) {
			final Entity entity = Minecraft.getInstance().getRenderViewEntity();

			if (entity == null) {
				return;
			}

			ActiveRenderInfo renderInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
			Vec3d view = renderInfo.getProjectedView();
			double renderPosX = view.x;
			double renderPosY = view.y;
			double renderPosZ = view.z;

			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

			for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
				if (!usedBlockRenderLayers[blockRenderLayerId]) {
					continue;
				}
				final BufferBuilder bufferBuilder = bufferCache.getBuilder(blockRenderLayerId);
				GlStateManager.pushMatrix();
				GlStateManager.translated(-renderPosX, -renderPosY, -renderPosZ);
				drawBuffer(bufferBuilder);
				GlStateManager.popMatrix();
			}
			GlStateManager.disableAlphaTest();
			GlStateManager.disableBlend();
		}
	}

	// Coppied from the Tesselator's vboUploader - Draw everything but don't
	// reset the buffer
	private static void drawBuffer(final BufferBuilder bufferBuilder) {
		if (bufferBuilder.getVertexCount() > 0) {

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

}
