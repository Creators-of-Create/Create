package com.simibubi.create.content.contraptions.components.structureMovement.render;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.WorldAttached;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ContraptionRenderDispatcher {
	private static final Lazy<BlockModelRenderer> MODEL_RENDERER = Lazy.of(() -> new BlockModelRenderer(Minecraft.getInstance().getBlockColors()));
	private static final Lazy<BlockModelShapes> BLOCK_MODELS = Lazy.of(() -> Minecraft.getInstance().getModelManager().getBlockModelShaper());

	private static final WorldAttached<WorldContraptions> WORLDS = new WorldAttached<>(WorldContraptions::new);

	public static final Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new Compartment<>();

	public static void tick(World world) {
		if (Minecraft.getInstance().isPaused()) return;

		WORLDS.get(world).tick();
	}

	@SubscribeEvent
	public static void beginFrame(BeginFrameEvent event) {
		WORLDS.get(event.getWorld()).beginFrame(event);
	}

	@SubscribeEvent
	public static void renderLayer(RenderLayerEvent event) {
		WORLDS.get(event.getWorld()).renderLayer(event);
	}

	@SubscribeEvent
	public static void onRendererReload(ReloadRenderersEvent event) {
		invalidateAll();
	}

	public static void invalidateOnGatherContext(GatherContextEvent e) {
		invalidateAll();
	}

	public static void render(AbstractContraptionEntity entity, Contraption contraption, IRenderTypeBuffer buffers) {
		World world = entity.level;

		ContraptionRenderInfo renderInfo = WORLDS.get(world)
				.getRenderInfo(contraption);

		renderDynamic(world, renderInfo.renderWorld, contraption, renderInfo.getMatrices(), buffers);
	}

	public static PlacementSimulationWorld setupRenderWorld(World world, Contraption c) {
		PlacementSimulationWorld renderWorld = new PlacementSimulationWorld(world);

		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (Template.BlockInfo info : c.getBlocks()
				.values())
			// Skip individual lighting updates to prevent lag with large contraptions
			renderWorld.setBlock(info.pos, info.state, 128);

		renderWorld.updateLightSources();
		renderWorld.lighter.runUpdates(Integer.MAX_VALUE, false, false);

		return renderWorld;
	}

	public static void renderDynamic(World world, PlacementSimulationWorld renderWorld, Contraption c,
									 ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		renderTileEntities(world, renderWorld, c, matrices, buffer);
		if (buffer instanceof IRenderTypeBuffer.Impl)
			((IRenderTypeBuffer.Impl) buffer).endBatch();
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
			m.pushPose();
			MatrixTransformStack.of(m)
					.translate(blockInfo.pos);

			MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);
			if (movementBehaviour != null)
				movementBehaviour.renderInContraption(context, renderWorld, matrices, buffer);

			m.popPose();
		}
	}

	public static SuperByteBuffer buildStructureBuffer(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		BufferBuilder builder = buildStructure(renderWorld, c, layer);
		return new SuperByteBuffer(builder);
	}

	public static BufferBuilder buildStructure(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		MatrixStack ms = new MatrixStack();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL_QUADS, DefaultVertexFormats.BLOCK);

		ForgeHooksClient.setRenderLayer(layer);
		BlockModelRenderer.enableCaching();
		for (Template.BlockInfo info : c.getBlocks()
				.values()) {
			BlockState state = info.state;

			if (state.getRenderShape() != BlockRenderType.MODEL)
				continue;
			if (!RenderTypeLookup.canRenderInLayer(state, layer))
				continue;

			BlockPos pos = info.pos;

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			MODEL_RENDERER.get().renderModel(renderWorld, BLOCK_MODELS.get().getBlockModel(state), state, pos, ms, builder, true,
					random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			ms.popPose();
		}
		BlockModelRenderer.clearCache();
		ForgeHooksClient.setRenderLayer(null);

		builder.end();
		return builder;
	}

	public static int getLight(World world, float lx, float ly, float lz) {
		BlockPos.Mutable pos = new BlockPos.Mutable();
		float block = 0, sky = 0;
		float offset = 1 / 8f;

		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.set(lx + xOffset, ly + yOffset, lz + zOffset);
					block += world.getBrightness(LightType.BLOCK, pos) / 8f;
					sky += world.getBrightness(LightType.SKY, pos) / 8f;
				}

		return LightTexture.pack((int) block, (int) sky);
	}

	public static int getContraptionWorldLight(MovementContext context, PlacementSimulationWorld renderWorld) {
		return WorldRenderer.getLightColor(renderWorld, context.localPos);
	}

	public static void invalidateAll() {
		WORLDS.empty(WorldContraptions::invalidate);
	}
}
