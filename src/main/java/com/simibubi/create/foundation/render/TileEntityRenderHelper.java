package com.simibubi.create.foundation.render;

import java.util.Iterator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.World;

public class TileEntityRenderHelper {

	public static void renderTileEntities(World world, Iterable<TileEntity> customRenderTEs, MatrixStack ms,
		MatrixStack localTransform, IRenderTypeBuffer buffer) {
		renderTileEntities(world, null, customRenderTEs, ms, localTransform, buffer);
	}

	public static void renderTileEntities(World world, Iterable<TileEntity> customRenderTEs, MatrixStack ms,
		MatrixStack localTransform, IRenderTypeBuffer buffer, float pt) {
		renderTileEntities(world, null, customRenderTEs, ms, localTransform, buffer, pt);
	}

	public static void renderTileEntities(World world, PlacementSimulationWorld renderWorld,
		Iterable<TileEntity> customRenderTEs, MatrixStack ms, MatrixStack localTransform, IRenderTypeBuffer buffer) {
		renderTileEntities(world, renderWorld, customRenderTEs, ms, localTransform, buffer,
			AnimationTickHolder.getPartialTicks());
	}

	public static void renderTileEntities(World world, PlacementSimulationWorld renderWorld,
		Iterable<TileEntity> customRenderTEs, MatrixStack ms, MatrixStack localTransform, IRenderTypeBuffer buffer,
		float pt) {
		Matrix4f matrix = localTransform.peek()
			.getModel();

		for (Iterator<TileEntity> iterator = customRenderTEs.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			// if (tileEntity instanceof IInstanceRendered) continue; // TODO: some things still need to render

			TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
			if (renderer == null) {
				iterator.remove();
				continue;
			}

			try {
				BlockPos pos = tileEntity.getPos();
				ms.push();
				MatrixStacker.of(ms)
					.translate(pos);

				Vector4f vec = new Vector4f(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1);
				vec.transform(matrix);
				BlockPos lightPos = new BlockPos(vec.getX(), vec.getY(), vec.getZ());
				int worldLight = ContraptionRenderDispatcher.getLightOnContraption(world, renderWorld, pos, lightPos);

				renderer.render(tileEntity, pt, ms, buffer, worldLight, OverlayTexture.DEFAULT_UV);
				ms.pop();

			} catch (Exception e) {
				iterator.remove();

				String message = "TileEntity " + tileEntity.getType()
					.getRegistryName()
					.toString() + " didn't want to render while moved.\n";
				if (AllConfigs.CLIENT.explainRenderErrors.get()) {
					Create.logger.error(message, e);
					continue;
				}

				Create.logger.error(message);
				continue;
			}
		}
	}

}
