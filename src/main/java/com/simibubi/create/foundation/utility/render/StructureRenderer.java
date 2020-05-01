package com.simibubi.create.foundation.utility.render;

import java.util.Iterator;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.Create;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WrappedWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class StructureRenderer {

	protected static LightingWorld lightingWorld;

	public static void renderTileEntities(World world, Vec3d position, Vec3d rotation,
			Iterable<TileEntity> customRenderTEs) {
		TileEntityRendererDispatcher dispatcher = TileEntityRendererDispatcher.instance;
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		World prevDispatcherWorld = dispatcher.world;

		if (lightingWorld == null)
			lightingWorld = new LightingWorld(world);
		lightingWorld.setWorld(world);
		lightingWorld.setTransform(position, rotation);
		dispatcher.setWorld(lightingWorld);

		for (Iterator<TileEntity> iterator = customRenderTEs.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			if (dispatcher.getRenderer(tileEntity) == null) {
				iterator.remove();
				continue;
			}

			try {

				BlockPos pos = tileEntity.getPos();
				if (!tileEntity.hasFastRenderer()) {
					RenderHelper.enableStandardItemLighting();
					int i = lightingWorld.getCombinedLight(pos, 0);
					int j = i % 65536;
					int k = i / 65536;
					GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) j, (float) k);
					GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				}

				World prevTileWorld = tileEntity.getWorld();
				tileEntity.setWorld(lightingWorld);
				GlStateManager.disableCull();
				dispatcher.render(tileEntity, pos.getX(), pos.getY(), pos.getZ(), pt, -1, true);
				GlStateManager.enableCull();
				tileEntity.setWorld(prevTileWorld);

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

		dispatcher.setWorld(prevDispatcherWorld);
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
		public int getCombinedLight(BlockPos pos, int minLight) {
			return super.getCombinedLight(transformPos(pos), minLight);
		}

		private BlockPos transformPos(BlockPos pos) {
			Vec3d vec = VecHelper.getCenterOf(pos);
			vec = VecHelper.rotate(vec, rotation.x, rotation.y, rotation.z);
			vec = vec.add(offset).subtract(VecHelper.getCenterOf(BlockPos.ZERO));
			return new BlockPos(vec);
		}

	}

}
