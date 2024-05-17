package com.simibubi.create.foundation.render;

import java.util.Iterator;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.impl.visualization.VisualizationHelper;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityRenderHelper {

	public static void renderBlockEntities(Level world, Iterable<BlockEntity> customRenderBEs, PoseStack ms,
			MultiBufferSource buffer) {
		renderBlockEntities(world, null, customRenderBEs, ms, null, buffer);
	}

	public static void renderBlockEntities(Level world, Iterable<BlockEntity> customRenderBEs, PoseStack ms,
			MultiBufferSource buffer, float pt) {
		renderBlockEntities(world, null, customRenderBEs, ms, null, buffer, pt);
	}

	public static void renderBlockEntities(Level world, @Nullable VirtualRenderWorld renderWorld,
			Iterable<BlockEntity> customRenderBEs, PoseStack ms, @Nullable Matrix4f lightTransform, MultiBufferSource buffer) {
		renderBlockEntities(world, renderWorld, customRenderBEs, ms, lightTransform, buffer,
			AnimationTickHolder.getPartialTicks());
	}

	public static void renderBlockEntities(Level world, @Nullable VirtualRenderWorld renderWorld,
			Iterable<BlockEntity> customRenderBEs, PoseStack ms, @Nullable Matrix4f lightTransform, MultiBufferSource buffer,
			float pt) {
		Iterator<BlockEntity> iterator = customRenderBEs.iterator();
		while (iterator.hasNext()) {
			BlockEntity blockEntity = iterator.next();
			if (VisualizationManager.supportsVisualization(world) && VisualizationHelper.shouldSkipRender(blockEntity))
				continue;

			BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
			if (renderer == null) {
				iterator.remove();
				continue;
			}

			BlockPos pos = blockEntity.getBlockPos();
			ms.pushPose();
			TransformStack.of(ms)
				.translate(pos);

			try {
				int worldLight = getCombinedLight(world, getLightPos(lightTransform, pos), renderWorld, pos);

				if (renderWorld != null) {
					// Swap the real world for the render world so that the renderer gets contraption-local information
					blockEntity.setLevel(renderWorld);
					renderer.render(blockEntity, pt, ms, buffer, worldLight, OverlayTexture.NO_OVERLAY);
					blockEntity.setLevel(world);
				} else {
					renderer.render(blockEntity, pt, ms, buffer, worldLight, OverlayTexture.NO_OVERLAY);
				}

			} catch (Exception e) {
				iterator.remove();

				String message = "BlockEntity " + RegisteredObjects.getKeyOrThrow(blockEntity.getType())
					.toString() + " could not be rendered virtually.";
				if (AllConfigs.client().explainRenderErrors.get())
					Create.LOGGER.error(message, e);
				else
					Create.LOGGER.error(message);
			}

			ms.popPose();
		}
	}

	private static BlockPos getLightPos(@Nullable Matrix4f lightTransform, BlockPos contraptionPos) {
		if (lightTransform != null) {
			Vector4f lightVec = new Vector4f(contraptionPos.getX() + .5f, contraptionPos.getY() + .5f, contraptionPos.getZ() + .5f, 1);
			lightVec.mul(lightTransform);
			return BlockPos.containing(lightVec.x(), lightVec.y(), lightVec.z());
		} else {
			return contraptionPos;
		}
	}

	public static int getCombinedLight(Level world, BlockPos worldPos, @Nullable VirtualRenderWorld renderWorld,
			BlockPos renderWorldPos) {
		int worldLight = LevelRenderer.getLightColor(world, worldPos);

		if (renderWorld != null) {
			int renderWorldLight = LevelRenderer.getLightColor(renderWorld, renderWorldPos);
			return SuperByteBuffer.maxLight(worldLight, renderWorldLight);
		}

		return worldLight;
	}

}
