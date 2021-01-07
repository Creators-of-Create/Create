package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.render.SuperByteBufferCache.Compartment;
import com.simibubi.create.foundation.utility.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ContraptionRenderer {

	public static final Compartment<Pair<Contraption, Integer>> CONTRAPTION = new Compartment<>();
	protected static PlacementSimulationWorld renderWorld;

	public static void render(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
							  IRenderTypeBuffer buffer) {
		renderDynamic(world, c, ms, msLocal, buffer);
		renderStructure(world, c, ms, msLocal, buffer);
	}

	public static void renderDynamic(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
									 IRenderTypeBuffer buffer) {
		renderTileEntities(world, c, ms, msLocal, buffer);
		if (buffer instanceof IRenderTypeBuffer.Impl)
			((IRenderTypeBuffer.Impl) buffer).draw();
		renderActors(world, c, ms, msLocal, buffer);
	}

	protected static void renderStructure(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		List<RenderType> blockLayers = RenderType.getBlockLayers();

		buffer.getBuffer(RenderType.getSolid());
		for (int i = 0; i < blockLayers.size(); i++) {
			RenderType layer = blockLayers.get(i);
			Pair<Contraption, Integer> key = Pair.of(c, i);
			SuperByteBuffer contraptionBuffer = bufferCache.get(CONTRAPTION, key, () -> buildStructureBuffer(c, layer));
			if (contraptionBuffer.isEmpty())
				continue;
			Matrix4f model = msLocal.peek()
				.getModel();
			contraptionBuffer.light(model)
				.renderInto(ms, buffer.getBuffer(layer));
		}
	}

	protected static void renderTileEntities(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		TileEntityRenderHelper.renderTileEntities(world, c.renderedTileEntities, ms, msLocal, buffer);
	}

	private static SuperByteBuffer buildStructureBuffer(Contraption c, RenderType layer) {
		BufferBuilder builder = buildStructure(c, layer);
		return new SuperByteBuffer(builder);
	}

	protected static BufferBuilder buildStructure(Contraption c, RenderType layer) {
		if (renderWorld == null || renderWorld.getWorld() != Minecraft.getInstance().world)
			renderWorld = new PlacementSimulationWorld(Minecraft.getInstance().world);

		ForgeHooksClient.setRenderLayer(layer);
		MatrixStack ms = new MatrixStack();
		BlockRendererDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRendererDispatcher();
		BlockModelRenderer blockRenderer = dispatcher.getBlockModelRenderer();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (BlockInfo info : c.getBlocks()
							   .values())
			renderWorld.setBlockState(info.pos, info.state);
		for (BlockInfo info : c.getBlocks()
							   .values()) {
			BlockState state = info.state;

			if (state.getRenderType() == BlockRenderType.ENTITYBLOCK_ANIMATED)
				continue;
			if (!RenderTypeLookup.canRenderInLayer(state, layer))
				continue;

			IBakedModel originalModel = dispatcher.getModelForState(state);
			ms.push();
			ms.translate(info.pos.getX(), info.pos.getY(), info.pos.getZ());
			blockRenderer.renderModel(renderWorld, originalModel, state, info.pos, ms, builder, true, random, 42,
				OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
			ms.pop();
		}

		builder.finishDrawing();
		renderWorld.clear();
		return builder;
	}

	protected static void renderActors(World world, Contraption c, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };
		for (Pair<BlockInfo, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = world;
			BlockInfo blockInfo = actor.getLeft();
			for (MatrixStack m : matrixStacks) {
				m.push();
				MatrixStacker.of(m)
					.translate(blockInfo.pos);
			}

			MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);
			if (movementBehaviour != null)
				movementBehaviour.renderInContraption(context, ms, msLocal, buffer);

			for (MatrixStack m : matrixStacks)
				m.pop();
		}
	}

	public static int getLight(World world, float lx, float ly, float lz) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		float sky = 0, block = 0;
		float offset = 1 / 8f;

		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.setPos(lx + xOffset, ly + yOffset, lz + zOffset);
					sky += world.getLightLevel(LightType.SKY, pos) / 8f;
					block += world.getLightLevel(LightType.BLOCK, pos) / 8f;
				}

		return ((int) sky) << 20 | ((int) block) << 4;
	}

}
