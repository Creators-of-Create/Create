package com.simibubi.create.foundation.render;

import java.util.BitSet;
import java.util.List;

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
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityRenderHelper {
	/**
	 * Renders the given list of BlockEntities, skipping those not marked in shouldRenderBEs,
	 * and marking those that error in erroredBEsOut.
	 *
	 * @param blockEntities   The list of BlockEntities to render.
	 * @param shouldRenderBEs A BitSet marking which BlockEntities in the list should be rendered. This will not be modified.
	 * @param erroredBEsOut   A BitSet to mark BlockEntities that error during rendering. This will be modified.
	 */
	public static void renderBlockEntities(List<BlockEntity> blockEntities, BitSet shouldRenderBEs, BitSet erroredBEsOut, @javax.annotation.Nullable VirtualRenderWorld renderLevel, Level realLevel, PoseStack ms, @javax.annotation.Nullable Matrix4f lightTransform, MultiBufferSource buffer,
										   float pt) {
		for (int i = shouldRenderBEs.nextSetBit(0); i >= 0 && i < blockEntities.size(); i = shouldRenderBEs.nextSetBit(i + 1)) {
			BlockEntity blockEntity = blockEntities.get(i);
			if (VisualizationManager.supportsVisualization(realLevel) && VisualizationHelper.skipVanillaRender(blockEntity))
				continue;

			BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance()
				.getBlockEntityRenderDispatcher()
				.getRenderer(blockEntity);
			if (renderer == null) {
				// Don't bother looping over it again if we can't do anything with it.
				erroredBEsOut.set(i);
				continue;
			}

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
				erroredBEsOut.set(i);

				String message = "BlockEntity " + RegisteredObjectsHelper.getKeyOrThrow(blockEntity.getType()) + " could not be rendered virtually.";
				if (AllConfigs.client().explainRenderErrors.get()) Create.LOGGER.error(message, e);
				else Create.LOGGER.error(message);
			}

			ms.popPose();
		}

		if (renderLevel != null) {
			renderLevel.resetExternalLight();
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
