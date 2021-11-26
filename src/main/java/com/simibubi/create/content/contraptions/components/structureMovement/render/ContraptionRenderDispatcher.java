package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.error.GlError;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.WorldAttached;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.TileEntityRenderHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ContraptionRenderDispatcher {

	private static WorldAttached<ContraptionRenderManager<?>> WORLDS = new WorldAttached<>(SBBContraptionManager::new);

	/**
	 * Reset a contraption's renderer.
	 * @param contraption The contraption to invalidate.
	 * @return true if there was a renderer associated with the given contraption.
	 */
	public static boolean invalidate(Contraption contraption) {
		Level level = contraption.entity.level;

		return WORLDS.get(level).invalidate(contraption);
	}

	public static void tick(Level world) {
		if (Minecraft.getInstance().isPaused()) return;

		WORLDS.get(world).tick();
	}

	public static void beginFrame(BeginFrameEvent event) {
		WORLDS.get(event.getWorld()).beginFrame(event);
	}

	public static void renderLayer(RenderLayerEvent event) {
		WORLDS.get(event.getWorld()).renderLayer(event);

		GlError.poll();//() -> "contraption layer: " + event.getLayer());
	}

	public static void onRendererReload(ReloadRenderersEvent event) {
		reset();
	}

	public static void gatherContext(GatherContextEvent e) {
		reset();
	}

	public static void renderFromEntity(AbstractContraptionEntity entity, Contraption contraption, MultiBufferSource buffers) {
		Level world = entity.level;

		ContraptionRenderInfo renderInfo = WORLDS.get(world)
				.getRenderInfo(contraption);
		ContraptionMatrices matrices = renderInfo.getMatrices();

		// something went wrong with the other rendering
		if (!matrices.isReady()) return;

		PlacementSimulationWorld renderWorld = renderInfo.renderWorld;

		renderTileEntities(world, renderWorld, contraption, matrices, buffers);

		if (buffers instanceof MultiBufferSource.BufferSource)
			((MultiBufferSource.BufferSource) buffers).endBatch();

		renderActors(world, renderWorld, contraption, matrices, buffers);
	}

	public static PlacementSimulationWorld setupRenderWorld(Level world, Contraption c) {
		PlacementSimulationWorld renderWorld = new PlacementSimulationWorld(world);

		renderWorld.setTileEntities(c.presentTileEntities.values());

		for (StructureTemplate.StructureBlockInfo info : c.getBlocks()
				.values())
			// Skip individual lighting updates to prevent lag with large contraptions
			renderWorld.setBlock(info.pos, info.state, Block.UPDATE_SUPPRESS_LIGHT);

		renderWorld.updateLightSources();
		renderWorld.lighter.runUpdates(Integer.MAX_VALUE, false, false);

		return renderWorld;
	}

	public static void renderTileEntities(Level world, PlacementSimulationWorld renderWorld, Contraption c,
										  ContraptionMatrices matrices, MultiBufferSource buffer) {
		TileEntityRenderHelper.renderTileEntities(world, renderWorld, c.specialRenderedTileEntities,
				matrices.getModelViewProjection(), matrices.getLight(), buffer);
	}

	protected static void renderActors(Level world, PlacementSimulationWorld renderWorld, Contraption c,
									   ContraptionMatrices matrices, MultiBufferSource buffer) {
		PoseStack m = matrices.getModel();

		for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = world;
			StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();

			MovementBehaviour movementBehaviour = AllMovementBehaviours.of(blockInfo.state);
			if (movementBehaviour != null) {
				m.pushPose();
				TransformStack.cast(m)
						.translate(blockInfo.pos);
				movementBehaviour.renderInContraption(context, renderWorld, matrices, buffer);
				m.popPose();
			}
		}
	}

	public static SuperByteBuffer buildStructureBuffer(PlacementSimulationWorld renderWorld, Contraption c, RenderType layer) {
		Collection<StructureTemplate.StructureBlockInfo> values = c.getBlocks()
				.values();
		BufferBuilder builder = ModelUtil.getBufferBuilderFromTemplate(renderWorld, layer, values);
		return new SuperByteBuffer(builder);
	}

	public static int getLight(Level world, float lx, float ly, float lz) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		float block = 0, sky = 0;
		float offset = 1 / 8f;

		for (float zOffset = offset; zOffset >= -offset; zOffset -= 2 * offset)
			for (float yOffset = offset; yOffset >= -offset; yOffset -= 2 * offset)
				for (float xOffset = offset; xOffset >= -offset; xOffset -= 2 * offset) {
					pos.set(lx + xOffset, ly + yOffset, lz + zOffset);
					block += world.getBrightness(LightLayer.BLOCK, pos) / 8f;
					sky += world.getBrightness(LightLayer.SKY, pos) / 8f;
				}

		return LightTexture.pack((int) block, (int) sky);
	}

	public static int getContraptionWorldLight(MovementContext context, PlacementSimulationWorld renderWorld) {
		return LevelRenderer.getLightColor(renderWorld, context.localPos);
	}

	public static void reset() {
		WORLDS.empty(ContraptionRenderManager::delete);

		if (Backend.getInstance().available()) {
			WORLDS = new WorldAttached<>(FlwContraptionManager::new);
		} else {
			WORLDS = new WorldAttached<>(SBBContraptionManager::new);
		}
	}
}
