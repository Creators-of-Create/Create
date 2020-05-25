package com.simibubi.create.foundation.utility;

import java.util.Iterator;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.crash.ReportedException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class TileEntityRenderHelper {

	protected static LightingWorld lightingWorld;

	public static void renderTileEntities(World world, Iterable<TileEntity> customRenderTEs, MatrixStack ms,
		MatrixStack localTransform, IRenderTypeBuffer buffer) {
		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();

		if (lightingWorld == null)
			lightingWorld = new LightingWorld(world);
		lightingWorld.setWorld(world);
		lightingWorld.setTransform(localTransform.peek()
			.getModel());

		for (Iterator<TileEntity> iterator = customRenderTEs.iterator(); iterator.hasNext();) {
			TileEntity tileEntity = iterator.next();
			if (TileEntityRendererDispatcher.instance.getRenderer(tileEntity) == null) {
				iterator.remove();
				continue;
			}

			try {

				BlockPos pos = tileEntity.getPos();
				World prevTileWorld = tileEntity.getWorld();
				ms.push();
				MatrixStacker.of(ms)
					.translate(pos);
				tileEntity.setLocation(lightingWorld, pos);
				TileEntityRendererDispatcher.instance.render(tileEntity, pt, ms, buffer);
				tileEntity.setLocation(prevTileWorld, pos);
				ms.pop();

			} catch (ReportedException e) {
				if (AllConfigs.CLIENT.explainRenderErrors.get()) {
					Create.logger.error("TileEntity " + tileEntity.getType()
						.getRegistryName()
						.toString() + " didn't want to render while moved.\n", e);
				} else {
					Create.logger.error("TileEntity " + tileEntity.getType()
						.getRegistryName()
						.toString() + " didn't want to render while moved.\n");
				}
				iterator.remove();
				continue;
			}
		}
	}

	private static class LightingWorld extends WrappedWorld {

		private Matrix4f matrix;

		public LightingWorld(World world) {
			super(world);
		}

		void setWorld(World world) {
			this.world = world;
		}

		void setTransform(Matrix4f matrix) {
			this.matrix = matrix;
		}

		@Override
		public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_) {
			return super.getLightLevel(p_226658_1_, transformPos(p_226658_2_));
		}

		private BlockPos transformPos(BlockPos pos) {
			Vector4f vec = new Vector4f(pos.getX(), pos.getY(), pos.getZ(), 1);
			vec.transform(matrix);
			return new BlockPos(vec.getX(), vec.getY(), vec.getZ());
		}

	}

}
