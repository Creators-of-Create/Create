package com.simibubi.create.foundation.render;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.infrastructure.config.AllConfigs;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

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

	public static void renderBlockEntities(Level realLevel, @Nullable VirtualRenderWorld renderLevel,
										   Iterable<BlockEntity> customRenderBEs, PoseStack ms, @Nullable Matrix4f lightTransform, MultiBufferSource buffer,
										   float pt) {
		// First, make sure all BEs have the render level.
		// Need to do this outside of the main loop in case BEs query the level from other virtual BEs.
		// e.g. double chests specifically fetch light from both their own and their neighbor's level,
		// which is honestly kind of silly, but easy to work around here.
		if (renderLevel != null) {
			for (var be : customRenderBEs) {
				be.setLevel(renderLevel);
			}
		}

		Set<BlockEntity> toRemove = new HashSet<>();

		// Main loop, time to render.
		for (BlockEntity blockEntity : customRenderBEs) {
			if (VisualizationManager.supportsVisualization(realLevel) && VisualizationHelper.skipVanillaRender(blockEntity))
				continue;

			BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance()
				.getBlockEntityRenderDispatcher()
				.getRenderer(blockEntity);
			if (renderer == null) {
				// Don't bother looping over it again if we can't do anything with it.
				toRemove.add(blockEntity);
				continue;
			}

			Vec3 cameraPos = Minecraft.getInstance()
				.gameRenderer
				.getMainCamera()
				.getPosition();

			if (realLevel instanceof SchematicLevel)
				cameraPos = Vec3.ZERO;

			if (renderLevel == null && !renderer.shouldRender(blockEntity, cameraPos))
				continue;

			BlockPos pos = blockEntity.getBlockPos();
			ms.pushPose();
			TransformStack.of(ms)
				.translate(pos);

			try {
				int realLevelLight = LevelRenderer.getLightColor(realLevel, getLightPos(lightTransform, pos));

				int light;
				if (renderLevel != null) {
					renderLevel.setExternalLight(realLevelLight);
					light = LevelRenderer.getLightColor(renderLevel, pos);
				} else {
					light = realLevelLight;
				}

				renderer.render(blockEntity, pt, ms, buffer, light, OverlayTexture.NO_OVERLAY);

			} catch (Exception e) {
				// Prevent this BE from causing more issues in the future.
				toRemove.add(blockEntity);

				String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType())
					.toString() + " could not be rendered virtually.";
				if (AllConfigs.client().explainRenderErrors.get()) Create.LOGGER.error(message, e);
				else Create.LOGGER.error(message);
			}

			ms.popPose();
		}

		// Now reset all the BEs' levels.
		if (renderLevel != null) {
			renderLevel.resetExternalLight();

			for (var be : customRenderBEs) {
				be.setLevel(realLevel);
			}
		}

		// And finally, cull any BEs that misbehaved.
		if (!toRemove.isEmpty()) {
			var it = customRenderBEs.iterator();
			while (it.hasNext()) {
				if (toRemove.contains(it.next())) {
					it.remove();
				}
			}
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

}
