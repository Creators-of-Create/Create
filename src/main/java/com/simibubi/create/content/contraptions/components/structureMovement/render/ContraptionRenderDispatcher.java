package com.simibubi.create.content.contraptions.components.structureMovement.render;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.model.ModelUtil;
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ContraptionRenderDispatcher {

	private static WorldAttached<ContraptionRenderManager<?>> WORLDS = new WorldAttached<>(SBBContraptionManager::new);

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
		reset();
	}

	public static void gatherContext(GatherContextEvent e) {
		reset();
	}

	public static void renderFromEntity(AbstractContraptionEntity entity, Contraption contraption, IRenderTypeBuffer buffers) {
		World world = entity.level;

		ContraptionRenderInfo renderInfo = WORLDS.get(world)
				.getRenderInfo(contraption);
		ContraptionMatrices matrices = renderInfo.getMatrices();

		// something went wrong with the other rendering
		if (!matrices.isReady()) return;

		PlacementSimulationWorld renderWorld = renderInfo.renderWorld;

		renderTileEntities(world, renderWorld, contraption, matrices, buffers);

		if (buffers instanceof IRenderTypeBuffer.Impl)
			((IRenderTypeBuffer.Impl) buffers).endBatch();

		renderActors(world, renderWorld, contraption, matrices, buffers);
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

	public static void renderTileEntities(World world, PlacementSimulationWorld renderWorld, Contraption c,
										  ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		TileEntityRenderHelper.renderTileEntities(world, renderWorld, c.specialRenderedTileEntities,
				matrices.getModelViewProjection(), matrices.getLight(), buffer);
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

			MatrixStack m = matrices.getModel();
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
		Collection<Template.BlockInfo> values = c.getBlocks()
				.values();
		BufferBuilder builder = ModelUtil.getBufferBuilderFromTemplate(renderWorld, layer, values);
		return new SuperByteBuffer(builder);
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

	public static void reset() {
		WORLDS.empty(ContraptionRenderManager::delete);

		if (Backend.getInstance().available()) {
			WORLDS = new WorldAttached<>(FlwContraptionManager::new);
		} else {
			WORLDS = new WorldAttached<>(SBBContraptionManager::new);
		}
	}
}
