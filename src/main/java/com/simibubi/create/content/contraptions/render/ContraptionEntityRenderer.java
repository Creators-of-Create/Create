package com.simibubi.create.content.contraptions.render;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllMovementBehaviours;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity> extends EntityRenderer<C> {
	public ContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(C entity) {
		return null;
	}

	@Override
	public boolean shouldRender(C entity, Frustum frustum, double cameraX, double cameraY,
		double cameraZ) {
		if (entity.getContraption() == null)
			return false;
		if (!entity.isAliveOrStale())
			return false;
		if (!entity.isReadyForRender())
			return false;

		return super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
	}

	@Override
	public void render(C entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
		int overlay) {
		super.render(entity, yaw, partialTicks, poseStack, buffers, overlay);

		Contraption contraption = entity.getContraption();
		if (contraption == null) {
			return;
		}

		Level level = entity.level();
		ContraptionRenderInfo renderInfo = ContraptionRenderInfo.get(contraption);
		VirtualRenderWorld renderWorld = renderInfo.getRenderWorld();
		ContraptionMatrices matrices = renderInfo.getMatrices();
		matrices.setup(poseStack, entity);

		if (!VisualizationManager.supportsVisualization(level)) {
			for (RenderType renderType : RenderType.chunkBufferLayers()) {
				SuperByteBuffer sbb = renderInfo.getBuffer(renderType);
				if (!sbb.isEmpty()) {
					VertexConsumer vc = buffers.getBuffer(renderType);
					sbb.transform(matrices.getModel())
						.useLevelLight(level, matrices.getWorld())
						.renderInto(poseStack, vc);
				}
			}
		}

		renderBlockEntities(level, renderWorld, contraption, matrices, buffers);
		renderActors(level, renderWorld, contraption, matrices, buffers);

		matrices.clear();
	}

	private static void renderBlockEntities(Level level, VirtualRenderWorld renderWorld, Contraption c,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockEntityRenderHelper.renderBlockEntities(level, renderWorld, c.getRenderedBEs(),
			matrices.getModelViewProjection(), matrices.getLight(), buffer);
	}

	private static void renderActors(Level level, VirtualRenderWorld renderWorld, Contraption c,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		PoseStack m = matrices.getModel();

		for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : c.getActors()) {
			MovementContext context = actor.getRight();
			if (context == null)
				continue;
			if (context.world == null)
				context.world = level;
			StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();

			MovementBehaviour movementBehaviour = AllMovementBehaviours.getBehaviour(blockInfo.state());
			if (movementBehaviour != null) {
				if (c.isHiddenInPortal(blockInfo.pos()))
					continue;
				m.pushPose();
				TransformStack.of(m)
					.translate(blockInfo.pos());
				movementBehaviour.renderInContraption(context, renderWorld, matrices, buffer);
				m.popPose();
			}
		}
	}
}
