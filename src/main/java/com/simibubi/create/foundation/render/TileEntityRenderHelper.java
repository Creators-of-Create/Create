package com.simibubi.create.foundation.render;

import java.util.Iterator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;

import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.render.instancing.IInstanceRendered;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRenderHelper {

	public static void renderTileEntities(World world, Iterable<TileEntity> customRenderTEs, MatrixStack ms,
		MatrixStack localTransform, IRenderTypeBuffer buffer) {
		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();
		Matrix4f matrix = localTransform.peek()
			.getModel();

		for (Iterator<TileEntity> iterator = customRenderTEs.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			if (tileEntity instanceof IInstanceRendered) continue; // TODO: some things still need to render

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
				renderer.render(tileEntity, pt, ms, buffer, WorldRenderer.getLightmapCoordinates(world, lightPos),
					OverlayTexture.DEFAULT_UV);
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
