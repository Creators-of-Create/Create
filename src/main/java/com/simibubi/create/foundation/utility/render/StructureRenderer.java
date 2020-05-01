package com.simibubi.create.foundation.utility.render;

import java.util.Iterator;

import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.Create;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WrappedWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class StructureRenderer {

	protected static LightingWorld lightingWorld;

	public static void renderTileEntities(World world, Vec3d position, Vec3d rotation,
			Iterable<TileEntity> customRenderTEs, MatrixStack ms, IRenderTypeBuffer buffer) {
		float pt = Minecraft.getInstance().getRenderPartialTicks();

		if (lightingWorld == null)
			lightingWorld = new LightingWorld(world);
		lightingWorld.setWorld(world);
		lightingWorld.setTransform(position, rotation);

		for (Iterator<TileEntity> iterator = customRenderTEs.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			if (TileEntityRendererDispatcher.instance.getRenderer(tileEntity) == null) {
				iterator.remove();
				continue;
			}

			try {

				BlockPos pos = tileEntity.getPos();
				if (!tileEntity.hasFastRenderer()) {
					RenderHelper.enable();
					int i = WorldRenderer.getLightmapCoordinates(lightingWorld, pos);
					int j = LightTexture.getBlockLightCoordinates(i);
					int k = LightTexture.getSkyLightCoordinates(i);
					RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, (float) j, (float) k);
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				}

				RenderSystem.disableCull();
				World prevTileWorld = tileEntity.getWorld();
				tileEntity.setLocation(lightingWorld, pos);
				TileEntityRendererDispatcher.instance.render(tileEntity, pt, ms, buffer);
				tileEntity.setLocation(prevTileWorld, pos);
				RenderSystem.enableCull();

			} catch (ReportedException e) {
				if (AllConfigs.CLIENT.explainRenderErrors.get()) {
					Create.logger.error("TileEntity " + tileEntity.getType().getRegistryName().toString()
							+ " didn't want to render while moved.\n", e);
				} else {
					Create.logger.error("TileEntity " + tileEntity.getType().getRegistryName().toString()
							+ " didn't want to render while moved.\n");
				}
				iterator.remove();
				continue;
			}
		}
	}

	private static class LightingWorld extends WrappedWorld {

		private Vec3d offset;
		private Vec3d rotation;

		public LightingWorld(World world) {
			super(world);
		}

		void setWorld(World world) {
			this.world = world;
		}

		void setTransform(Vec3d offset, Vec3d rotation) {
			this.offset = offset;
			this.rotation = rotation;
		}

		@Override
		public int getBaseLightLevel(BlockPos pos, int minLight) {
			return super.getBaseLightLevel(transformPos(pos), minLight);
		}

		private BlockPos transformPos(BlockPos pos) {
			Vec3d vec = VecHelper.getCenterOf(pos);
			vec = VecHelper.rotate(vec, rotation.x, rotation.y, rotation.z);
			vec = vec.add(offset).subtract(VecHelper.getCenterOf(BlockPos.ZERO));
			return new BlockPos(vec);
		}

	}

}
