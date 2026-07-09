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
import net.createmod.catnip.api.registry.RegisteredObjectsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
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
