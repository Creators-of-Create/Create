package com.simibubi.create.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SchematicannonRenderer extends TileEntityRenderer<SchematicannonTileEntity> {

	@Override
	public void render(SchematicannonTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {

		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		double yaw = 0;
		double pitch = 40;
		double recoil = 0;

		BlockPos pos = tileEntityIn.getPos();
		if (tileEntityIn.target != null) {

			// Calculate Angle of Cannon
			Vec3d diff = new Vec3d(tileEntityIn.target.subtract(pos));
			if (tileEntityIn.previousTarget != null) {
				diff = (new Vec3d(tileEntityIn.previousTarget)
						.add(new Vec3d(tileEntityIn.target.subtract(tileEntityIn.previousTarget)).scale(partialTicks)))
								.subtract(new Vec3d(pos));
			}

			double diffX = diff.getX();
			double diffZ = diff.getZ();
			yaw = MathHelper.atan2(diffX, diffZ);
			yaw = yaw / Math.PI * 180;

			float distance = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
			double yOffset = 0 + distance * 2f;
			pitch = MathHelper.atan2(distance, diff.getY() * 3 + yOffset);
			pitch = pitch / Math.PI * 180 + 10;

		}

		if (!tileEntityIn.flyingBlocks.isEmpty()) {
			for (SchematicannonTileEntity.LaunchedBlock block : tileEntityIn.flyingBlocks) {

				if (block.ticksRemaining == 0)
					continue;
				
				// Calculate position of flying block
				Vec3d start = new Vec3d(tileEntityIn.getPos().add(.5f, 1, .5f));
				Vec3d target = new Vec3d(block.target).add(-.5, 0, 1);
				Vec3d distance = target.subtract(start);

				double targetY = target.y - start.y;
				double throwHeight = Math.sqrt(distance.lengthSquared()) * .6f + targetY;
				Vec3d cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
				start = start.add(cannonOffset);

				double progress = ((double) block.totalTicks - (block.ticksRemaining + 1 - partialTicks))
						/ block.totalTicks;
				Vec3d blockLocationXZ = new Vec3d(x + .5, y + .5, z + .5)
						.add(target.subtract(start).scale(progress).mul(1, 0, 1));

				// Height is determined through a bezier curve
				double t = progress;
				double yOffset = 2 * (1 - t) * t * throwHeight + t * t * targetY;
				Vec3d blockLocation = blockLocationXZ.add(0, yOffset + 1, 0).add(cannonOffset);

				// Offset to position
				GlStateManager.pushMatrix();
				GlStateManager.translated(blockLocation.x, blockLocation.y, blockLocation.z);

				// Rotation and Scaling effects
				double scale = .3f;
				GlStateManager.rotated(360 * t * 2, 1, 1, 0);
				GlStateManager.scaled(scale, scale, scale);

				// Render the Block
				Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(block.state, 1);
				GlStateManager.popMatrix();

//				Minecraft.getInstance().world.addParticle(ParticleTypes.END_ROD, blockLocation.x, blockLocation.y,
//						blockLocation.z, 0, 0, 0);

				// Apply Recoil if block was just launched
				if ((block.ticksRemaining + 1 - partialTicks) > block.totalTicks - 10) {
					recoil = Math.max(recoil, (block.ticksRemaining + 1 - partialTicks) - block.totalTicks + 10);
				}
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5f, y, z + 1 - .5f);
		GlStateManager.rotated(yaw, 0, 1, 0);
		GlStateManager.translated(-0.5f, 0, 0.5f);
		Minecraft.getInstance().getBlockRendererDispatcher()
				.renderBlockBrightness(AllBlocks.SCHEMATICANNON_CONNECTOR.get().getDefaultState(), 1);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translated(x + .5f, y + .90f, z + 1 - .5f);
		GlStateManager.rotated(yaw, 0, 1, 0);
		GlStateManager.rotated(pitch, 1, 0, 0);
		GlStateManager.translated(-0.5f, -.90f, 0.5f);

		GlStateManager.translated(0, -recoil / 100, 0);

		Minecraft.getInstance().getBlockRendererDispatcher()
				.renderBlockBrightness(AllBlocks.SCHEMATICANNON_PIPE.get().getDefaultState(), 1);
		GlStateManager.popMatrix();

		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);
	}

}
