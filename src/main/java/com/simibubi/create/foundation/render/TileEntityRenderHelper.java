package com.simibubi.create.foundation.render;

import java.util.Iterator;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileEntityRenderHelper {

	public static void renderTileEntities(Level world, Iterable<BlockEntity> customRenderTEs, PoseStack ms,
			MultiBufferSource buffer) {
		renderTileEntities(world, null, customRenderTEs, ms, null, buffer);
	}

	public static void renderTileEntities(Level world, Iterable<BlockEntity> customRenderTEs, PoseStack ms,
			MultiBufferSource buffer, float pt) {
		renderTileEntities(world, null, customRenderTEs, ms, null, buffer, pt);
	}

	public static void renderTileEntities(Level world, @Nullable VirtualRenderWorld renderWorld,
			Iterable<BlockEntity> customRenderTEs, PoseStack ms, @Nullable Matrix4f lightTransform, MultiBufferSource buffer) {
		renderTileEntities(world, renderWorld, customRenderTEs, ms, lightTransform, buffer,
			AnimationTickHolder.getPartialTicks());
	}

	public static void renderTileEntities(Level world, @Nullable VirtualRenderWorld renderWorld,
			Iterable<BlockEntity> customRenderTEs, PoseStack ms, @Nullable Matrix4f lightTransform, MultiBufferSource buffer,
			float pt) {
		Iterator<BlockEntity> iterator = customRenderTEs.iterator();
		while (iterator.hasNext()) {
			BlockEntity tileEntity = iterator.next();
			if (Backend.canUseInstancing(renderWorld) && InstancedRenderRegistry.getInstance()
					.shouldSkipRender(tileEntity)) continue;

			BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tileEntity);
			if (renderer == null) {
				iterator.remove();
				continue;
			}

			BlockPos pos = tileEntity.getBlockPos();
			ms.pushPose();
			TransformStack.cast(ms)
				.translate(pos);

			try {
				BlockPos lightPos;
				if (lightTransform != null) {
					Vector4f lightVec = new Vector4f(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1);
					lightVec.transform(lightTransform);
					lightPos = new BlockPos(lightVec.x(), lightVec.y(), lightVec.z());
				} else {
					lightPos = pos;
				}
				int worldLight = getCombinedLight(world, lightPos, renderWorld, pos);
				renderer.render(tileEntity, pt, ms, buffer, worldLight, OverlayTexture.NO_OVERLAY);

			} catch (Exception e) {
				iterator.remove();

				String message = "TileEntity " + tileEntity.getType()
					.getRegistryName()
					.toString() + " didn't want to render while moved.\n";
				if (AllConfigs.CLIENT.explainRenderErrors.get())
					Create.LOGGER.error(message, e);
				else
					Create.LOGGER.error(message);
			}

			ms.popPose();
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
