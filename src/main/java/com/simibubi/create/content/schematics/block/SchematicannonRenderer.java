package com.simibubi.create.content.schematics.block;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.schematics.block.LaunchedItem.ForBlockState;
import com.simibubi.create.content.schematics.block.LaunchedItem.ForEntity;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicannonRenderer extends SafeTileEntityRenderer<SchematicannonTileEntity> {

	public SchematicannonRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean isGlobalRenderer(SchematicannonTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(SchematicannonTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
			IRenderTypeBuffer buffer, int light, int overlay) {

		boolean blocksLaunching = !tileEntityIn.flyingBlocks.isEmpty();
		if (blocksLaunching)
			renderLaunchedBlocks(tileEntityIn, partialTicks, ms, buffer, light, overlay);

		if (FastRenderDispatcher.available(tileEntityIn.getWorld())) return;

		BlockPos pos = tileEntityIn.getPos();

		double[] cannonAngles = getCannonAngles(tileEntityIn, pos, partialTicks);

		double pitch = cannonAngles[0];
		double yaw = cannonAngles[1];

		double recoil = getRecoil(tileEntityIn, partialTicks);

		ms.push();
		BlockState state = tileEntityIn.getBlockState();
		int lightCoords = WorldRenderer.getLightmapCoordinates(tileEntityIn.getWorld(), pos);

		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		SuperByteBuffer connector = PartialBufferer.get(AllBlockPartials.SCHEMATICANNON_CONNECTOR, state);
		connector.translate(.5f, 0, .5f);
		connector.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
		connector.translate(-.5f, 0, -.5f);
		connector.light(lightCoords).renderInto(ms, vb);

		SuperByteBuffer pipe = PartialBufferer.get(AllBlockPartials.SCHEMATICANNON_PIPE, state);
		pipe.translate(.5f, 15 / 16f, .5f);
		pipe.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
		pipe.rotate(Direction.SOUTH, (float) (pitch / 180 * Math.PI));
		pipe.translate(-.5f, -15 / 16f, -.5f);
		pipe.translate(0, -recoil / 100, 0);
		pipe.light(lightCoords).renderInto(ms, vb);

		ms.pop();
	}

	public static double[] getCannonAngles(SchematicannonTileEntity tile, BlockPos pos, float partialTicks) {
		double yaw = 0;
		double pitch = 40;

		if (tile.target != null) {

			// Calculate Angle of Cannon
			Vector3d diff = Vector3d.of(tile.target.subtract(pos));
			if (tile.previousTarget != null) {
				diff = (Vector3d.of(tile.previousTarget)
						.add(Vector3d.of(tile.target.subtract(tile.previousTarget)).scale(partialTicks)))
						.subtract(Vector3d.of(pos));
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

		return new double[] { pitch, yaw };
	}

	public static double getRecoil(SchematicannonTileEntity tileEntityIn, float partialTicks) {
		double recoil = 0;

		for (LaunchedItem launched : tileEntityIn.flyingBlocks) {

			if (launched.ticksRemaining == 0) continue;

			// Apply Recoil if block was just launched
			if ((launched.ticksRemaining + 1 - partialTicks) > launched.totalTicks - 10)
				recoil = Math.max(recoil, (launched.ticksRemaining + 1 - partialTicks) - launched.totalTicks + 10);
		}

		return recoil;
	}

	private static void renderLaunchedBlocks(SchematicannonTileEntity tileEntityIn, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		for (LaunchedItem launched : tileEntityIn.flyingBlocks) {

			if (launched.ticksRemaining == 0)
				continue;

			// Calculate position of flying block
			Vector3d start = Vector3d.of(tileEntityIn.getPos().add(.5f, 1, .5f));
			Vector3d target = Vector3d.of(launched.target).add(-.5, 0, 1);
			Vector3d distance = target.subtract(start);

			double targetY = target.y - start.y;
			double throwHeight = Math.sqrt(distance.lengthSquared()) * .6f + targetY;
			Vector3d cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
			start = start.add(cannonOffset);

			float progress =
				((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
			Vector3d blockLocationXZ = new Vector3d(.5, .5, .5).add(target.subtract(start).scale(progress).mul(1, 0, 1));

			// Height is determined through a bezier curve
			float t = progress;
			double yOffset = 2 * (1 - t) * t * throwHeight + t * t * targetY;
			Vector3d blockLocation = blockLocationXZ.add(0, yOffset + 1, 0).add(cannonOffset);

			// Offset to position
			ms.push();
			ms.translate(blockLocation.x, blockLocation.y, blockLocation.z);

			ms.multiply(new Vector3f(0, 1, 0).getDegreesQuaternion(360 * t * 2));
			ms.multiply(new Vector3f(1, 0, 0).getDegreesQuaternion(360 * t * 2));

			// Render the Block
			if (launched instanceof ForBlockState) {
				float scale = .3f;
				ms.scale(scale, scale, scale);
				Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(((ForBlockState) launched).state, ms, buffer, light, overlay, EmptyModelData.INSTANCE);
			}

			// Render the item
			if (launched instanceof ForEntity) {
				float scale = 1.2f;
				ms.scale(scale, scale, scale);
				Minecraft.getInstance().getItemRenderer().renderItem(launched.stack, TransformType.GROUND, light, overlay, ms, buffer);
			}

			ms.pop();

			// Render particles for launch
			if (launched.ticksRemaining == launched.totalTicks && tileEntityIn.firstRenderTick) {
				tileEntityIn.firstRenderTick = false;
				for (int i = 0; i < 10; i++) {
					Random r = tileEntityIn.getWorld().getRandom();
					double sX = cannonOffset.x * .01f;
					double sY = (cannonOffset.y + 1) * .01f;
					double sZ = cannonOffset.z * .01f;
					double rX = r.nextFloat() - sX * 40;
					double rY = r.nextFloat() - sY * 40;
					double rZ = r.nextFloat() - sZ * 40;
					tileEntityIn.getWorld().addParticle(ParticleTypes.CLOUD, start.x + rX, start.y + rY,
														start.z + rZ, sX, sY, sZ);
				}
			}

		}
	}

}
