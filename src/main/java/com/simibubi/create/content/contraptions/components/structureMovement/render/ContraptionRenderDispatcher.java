package com.simibubi.create.content.contraptions.components.structureMovement.render;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ContraptionRenderDispatcher {
	private static final BlockModelRenderer MODEL_RENDERER = new BlockModelRenderer(Minecraft.getInstance().getBlockColors());
	private static final BlockModelShapes BLOCK_MODELS = Minecraft.getInstance().getModelManager().getBlockModelShapes();
	private static int worldHolderRefreshCounter;

	public static final Int2ObjectMap<RenderedContraption> RENDERERS = new Int2ObjectOpenHashMap<>();
	public static final Int2ObjectMap<ContraptionWorldHolder> WORLD_HOLDERS = new Int2ObjectOpenHashMap<>();
	public static final Compartment<Pair<Contraption, Integer>> CONTRAPTION = new Compartment<>();

	public static void tick() {
		if (Minecraft.getInstance().isGamePaused()) return;

		for (RenderedContraption contraption : RENDERERS.values()) {
			contraption.getLighter().tick(contraption);

			contraption.kinetics.tick();
		}

		worldHolderRefreshCounter++;
		if (worldHolderRefreshCounter >= 20) {
			removeDeadHolders();
			worldHolderRefreshCounter = 0;
		}
	}

	public static void beginFrame(ClientWorld world, MatrixStack stack, ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture) {
		double camX = info.getProjectedView().x;
		double camY = info.getProjectedView().y;
		double camZ = info.getProjectedView().z;
		for (RenderedContraption renderer : RENDERERS.values()) {
			renderer.beginFrame(info, camX, camY, camZ);
		}
	}

	public static void renderLayer(ClientWorld world, RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ) {
		removeDeadContraptions();

		if (RENDERERS.isEmpty()) return;

		layer.startDrawing();
		glEnable(GL_TEXTURE_3D);
		glActiveTexture(GL_TEXTURE4); // the shaders expect light volumes to be in texture 4

		if (Backend.canUseVBOs()) {
			ContraptionProgram structureShader = ContraptionContext.INSTANCE.getProgram(AllProgramSpecs.STRUCTURE);

			structureShader.bind();
			structureShader.uploadViewProjection(viewProjection);
			structureShader.uploadCameraPos(camX, camY, camZ);

			for (RenderedContraption renderer : RENDERERS.values()) {
				renderer.doRenderLayer(layer, structureShader);
			}
		}

		if (Backend.canUseInstancing()) {
			for (RenderedContraption renderer : RENDERERS.values()) {
				renderer.kinetics.render(layer, viewProjection, camX, camY, camZ, renderer::setup);
				renderer.teardown();
			}
		}

		layer.endDrawing();
		glDisable(GL_TEXTURE_3D);
		glActiveTexture(GL_TEXTURE0);
	}

	public static void render(AbstractContraptionEntity entity, Contraption contraption,
							  ContraptionMatrices matrices, IRenderTypeBuffer buffers) {
		World world = entity.world;
		if (Backend.canUseVBOs() && Backend.isFlywheelWorld(world)) {
			RenderedContraption renderer = getRenderer(world, contraption);
			PlacementSimulationWorld renderWorld = renderer.renderWorld;

			ContraptionRenderDispatcher.renderDynamic(world, renderWorld, contraption, matrices, buffers);
		} else {
			ContraptionWorldHolder holder = getWorldHolder(world, contraption);
			PlacementSimulationWorld renderWorld = holder.renderWorld;

			ContraptionRenderDispatcher.renderDynamic(world, renderWorld, contraption, matrices, buffers);
			ContraptionRenderDispatcher.renderStructure(world, renderWorld, contraption, matrices, buffers);
		}
	}

	private static RenderedContraption getRenderer(World world, Contraption c) {
		int entityId = c.entity.getEntityId();
		RenderedContraption contraption = RENDERERS.get(entityId);

		if (contraption == null) {
			PlacementSimulationWorld renderWorld = setupRenderWorld(world, c);
			contraption = new RenderedContraption(world, renderWorld, c);
			RENDERERS.put(entityId, contraption);
		}

		return contraption;
	}

	private static ContraptionWorldHolder getWorldHolder(World world, Contraption c) {
		int entityId = c.entity.getEntityId();
		ContraptionWorldHolder holder = WORLD_HOLDERS.get(entityId);

		if (holder == null) {
			PlacementSimulationWorld renderWorld = setupRenderWorld(world, c);
			holder = new ContraptionWorldHolder(c, renderWorld);
			WORLD_HOLDERS.put(entityId, holder);
		}

		return holder;
	}

	public static PlacementSimulationWorld setupRenderWorld(World world, Contraption c) {
		PlacementSimulationWorld renderWorld = new PlacementSimulationWorld(world);

		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (Template.BlockInfo info : c.getBlocks()
										.values())
			// Skip individual lighting updates to prevent lag with large contraptions
			renderWorld.setBlockState(info.pos, info.state, 128);

		renderWorld.updateLightSources();
		renderWorld.lighter.tick(Integer.MAX_VALUE, false, false);

		return renderWorld;
	}

	public static void renderDynamic(World world, PlacementSimulationWorld renderWorld, Contraption c,
									 ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		renderTileEntities(world, renderWorld, c, matrices, buffer);
		if (buffer instanceof IRenderTypeBuffer.Impl)
			((IRenderTypeBuffer.Impl) buffer).draw();
		renderActors(world, renderWorld, c, matrices, buffer);
	}

	public static void renderTileEntities(World world, PlacementSimulationWorld renderWorld, Contraption c,
										  ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		TileEntityRenderHelper.renderTileEntities(world, renderWorld, c.specialRenderedTileEntities,
				matrices.getFinalStack(), matrices.getFinalLight(), buffer);
	}

	protected static void renderActors(World world, PlacementSimulationWorld renderWorld, Contraption c,
									   ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		for (Pair<Template.BlockInfo, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = world;
			Template.BlockInfo blockInfo = actor.getLeft();

			MatrixStack m = matrices.contraptionStack;
			m.push();
			MatrixStacker.of(m)
				.translate(blockInfo.pos);

			MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);
			if (movementBehaviour != null)
				movementBehaviour.renderInContraption(context, renderWorld, matrices, buffer);

			m.pop();
		}
	}

	public static void renderStructure(World world, PlacementSimulationWorld renderWorld, Contraption c,
									   ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		SuperByteBufferCache bufferCache = CreateClient.bufferCache;
		List<RenderType> blockLayers = RenderType.getBlockLayers();

		buffer.getBuffer(RenderType.getSolid());
		for (int i = 0; i < blockLayers.size(); i++) {
			RenderType layer = blockLayers.get(i);
			Pair<Contraption, Integer> key = Pair.of(c, i);
			SuperByteBuffer contraptionBuffer = bufferCache.get(CONTRAPTION, key, () -> buildStructureBuffer(renderWorld, c, layer));
			if (contraptionBuffer.isEmpty())
				continue;
			contraptionBuffer
					.transform(matrices.contraptionStack)
					.light(matrices.entityMatrix)
					.hybridLight()
					.renderInto(matrices.entityStack, buffer.getBuffer(layer));
		}
	}

	private static SuperByteBuffer buildStructureBuffer(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		BufferBuilder builder = buildStructure(renderWorld, c, layer);
		return new SuperByteBuffer(builder);
	}

	public static BufferBuilder buildStructure(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		MatrixStack ms = new MatrixStack();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		ForgeHooksClient.setRenderLayer(layer);
		BlockModelRenderer.enableCache();
		for (Template.BlockInfo info : c.getBlocks()
										.values()) {
			BlockState state = info.state;

			if (state.getRenderType() != BlockRenderType.MODEL)
				continue;
			if (!RenderTypeLookup.canRenderInLayer(state, layer))
				continue;

			BlockPos pos = info.pos;

			ms.push();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			MODEL_RENDERER.renderModel(renderWorld, BLOCK_MODELS.getModel(state), state, pos, ms, builder, true,
									   random, 42, OverlayTexture.DEFAULT_UV, EmptyModelData.INSTANCE);
			ms.pop();
		}
		BlockModelRenderer.disableCache();
		ForgeHooksClient.setRenderLayer(null);

		builder.finishDrawing();
		return builder;
	}

	public static int getLight(World world, float lx, float ly, float lz) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		float block = 0, sky = 0;
		float offset = 1 / 8f;

		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.setPos(lx + xOffset, ly + yOffset, lz + zOffset);
					block += world.getLightLevel(LightType.BLOCK, pos) / 8f;
					sky += world.getLightLevel(LightType.SKY, pos) / 8f;
				}

		return LightTexture.pack((int) block, (int) sky);
	}

	public static int getContraptionWorldLight(MovementContext context, PlacementSimulationWorld renderWorld) {
		return WorldRenderer.getLightmapCoordinates(renderWorld, context.localPos);
	}

	public static void invalidateAll() {
		for (RenderedContraption renderer : RENDERERS.values()) {
			renderer.invalidate();
		}

		RENDERERS.clear();
		WORLD_HOLDERS.clear();
	}

	public static void removeDeadContraptions() {
		RENDERERS.values().removeIf(renderer -> {
			if (renderer.isDead()) {
				renderer.invalidate();
				return true;
			}
			return false;
		});
	}

	public static void removeDeadHolders() {
		WORLD_HOLDERS.values().removeIf(ContraptionWorldHolder::isDead);
	}
}
