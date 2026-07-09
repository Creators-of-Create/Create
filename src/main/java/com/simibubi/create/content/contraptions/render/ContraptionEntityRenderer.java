package com.simibubi.create.content.contraptions.render;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ClientContraption.RenderedBlocks;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity> extends EntityRenderer<C, EntityRenderState> {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public ContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public static SuperByteBuffer getBuffer(Contraption contraption, VirtualRenderWorld renderWorld, RenderType renderType) {
		return SuperByteBufferCache.getInstance().get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(contraption, renderWorld, renderType));
	}

	private static SuperByteBuffer buildStructureBuffer(Contraption contraption, VirtualRenderWorld renderWorld, RenderType layer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();
		// TODO 26.2: Port contraption block buffering to BlockStateModel/SubmitNodeCollector.
		return sbbBuilder.end();
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
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

	public void render(C entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
		int overlay) {
		// TODO 26.2: Port contraption rendering to EntityRenderer#submit.
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

			MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(blockInfo.state());
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

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final RandomSource random = RandomSource.createThreadLocalInstance();
		public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
	}
}
