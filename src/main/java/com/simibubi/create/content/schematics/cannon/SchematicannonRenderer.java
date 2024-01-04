package com.simibubi.create.content.schematics.cannon;

import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.schematics.cannon.LaunchedItem.ForBelt;
import com.simibubi.create.content.schematics.cannon.LaunchedItem.ForBlockState;
import com.simibubi.create.content.schematics.cannon.LaunchedItem.ForEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SchematicannonRenderer extends SafeBlockEntityRenderer<SchematicannonBlockEntity> {

	public SchematicannonRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(SchematicannonBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		boolean blocksLaunching = !blockEntity.flyingBlocks.isEmpty();
		if (blocksLaunching)
			renderLaunchedBlocks(blockEntity, partialTicks, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(blockEntity.getLevel()))
			return;

		BlockPos pos = blockEntity.getBlockPos();
		BlockState state = blockEntity.getBlockState();

		double[] cannonAngles = getCannonAngles(blockEntity, pos, partialTicks);

		double yaw = cannonAngles[0];
		double pitch = cannonAngles[1];

		double recoil = getRecoil(blockEntity, partialTicks);

		ms.pushPose();

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		SuperByteBuffer connector = CachedBufferer.partial(AllPartialModels.SCHEMATICANNON_CONNECTOR, state);
		connector.translate(.5f, 0, .5f);
		connector.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
		connector.translate(-.5f, 0, -.5f);
		connector.light(light)
			.renderInto(ms, vb);

		SuperByteBuffer pipe = CachedBufferer.partial(AllPartialModels.SCHEMATICANNON_PIPE, state);
		pipe.translate(.5f, 15 / 16f, .5f);
		pipe.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
		pipe.rotate((float) (pitch / 180 * Math.PI), Direction.SOUTH);
		pipe.translate(-.5f, -15 / 16f, -.5f);
		pipe.translate(0, -recoil / 100, 0);
		pipe.light(light)
			.renderInto(ms, vb);

		ms.popPose();
	}

	public static double[] getCannonAngles(SchematicannonBlockEntity blockEntity, BlockPos pos, float partialTicks) {
		double yaw;
		double pitch;

		BlockPos target = blockEntity.printer.getCurrentTarget();
		if (target != null) {

			// Calculate Angle of Cannon
			Vec3 diff = Vec3.atLowerCornerOf(target.subtract(pos));
			if (blockEntity.previousTarget != null) {
				diff = (Vec3.atLowerCornerOf(blockEntity.previousTarget)
					.add(Vec3.atLowerCornerOf(target.subtract(blockEntity.previousTarget))
						.scale(partialTicks))).subtract(Vec3.atLowerCornerOf(pos));
			}

			double diffX = diff.x();
			double diffZ = diff.z();
			yaw = Mth.atan2(diffX, diffZ);
			yaw = yaw / Math.PI * 180;

			float distance = Mth.sqrt((float) (diffX * diffX + diffZ * diffZ));
			double yOffset = 0 + distance * 2f;
			pitch = Mth.atan2(distance, diff.y() * 3 + yOffset);
			pitch = pitch / Math.PI * 180 + 10;

		} else {
			yaw = blockEntity.defaultYaw;
			pitch = 40;
		}

		return new double[] { yaw, pitch };
	}

	public static double getRecoil(SchematicannonBlockEntity blockEntity, float partialTicks) {
		double recoil = 0;

		for (LaunchedItem launched : blockEntity.flyingBlocks) {

			if (launched.ticksRemaining == 0)
				continue;

			// Apply Recoil if block was just launched
			if ((launched.ticksRemaining + 1 - partialTicks) > launched.totalTicks - 10)
				recoil = Math.max(recoil, (launched.ticksRemaining + 1 - partialTicks) - launched.totalTicks + 10);
		}

		return recoil;
	}

	private static void renderLaunchedBlocks(SchematicannonBlockEntity blockEntity, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		for (LaunchedItem launched : blockEntity.flyingBlocks) {

			if (launched.ticksRemaining == 0)
				continue;

			// Calculate position of flying block
			Vec3 start = Vec3.atCenterOf(blockEntity.getBlockPos()
				.above());
			Vec3 target = Vec3.atCenterOf(launched.target);
			Vec3 distance = target.subtract(start);

			double yDifference = target.y - start.y;
			double throwHeight = Math.sqrt(distance.lengthSqr()) * .6f + yDifference;
			Vec3 cannonOffset = distance.add(0, throwHeight, 0)
				.normalize()
				.scale(2);
			start = start.add(cannonOffset);
			yDifference = target.y - start.y;

			float progress =
				((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
			Vec3 blockLocationXZ = target.subtract(start)
				.scale(progress)
				.multiply(1, 0, 1);

			// Height is determined through a bezier curve
			float t = progress;
			double yOffset = 2 * (1 - t) * t * throwHeight + t * t * yDifference;
			Vec3 blockLocation = blockLocationXZ.add(0.5, yOffset + 1.5, 0.5)
				.add(cannonOffset);

			// Offset to position
			ms.pushPose();
			ms.translate(blockLocation.x, blockLocation.y, blockLocation.z);

			ms.translate(.125f, .125f, .125f);
			ms.mulPose(Axis.YP.rotationDegrees(360 * t));
			ms.mulPose(Axis.XP.rotationDegrees(360 * t));
			ms.translate(-.125f, -.125f, -.125f);

			if (launched instanceof ForBlockState) {
				// Render the Block
				BlockState state;
				if (launched instanceof ForBelt) {
					// Render a shaft instead of the belt
					state = AllBlocks.SHAFT.getDefaultState();
				} else {
					state = ((ForBlockState) launched).state;
				}
				float scale = .3f;
				ms.scale(scale, scale, scale);
				Minecraft.getInstance()
					.getBlockRenderer()
					.renderSingleBlock(state, ms, buffer, light, overlay,
						ModelUtil.VIRTUAL_DATA, null);
			} else if (launched instanceof ForEntity) {
				// Render the item
				float scale = 1.2f;
				ms.scale(scale, scale, scale);
				Minecraft.getInstance()
					.getItemRenderer()
					.renderStatic(launched.stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, blockEntity.getLevel(), 0);
			}

			ms.popPose();

			// Render particles for launch
			if (launched.ticksRemaining == launched.totalTicks && blockEntity.firstRenderTick) {
				start = start.subtract(.5, .5, .5);
				blockEntity.firstRenderTick = false;
				for (int i = 0; i < 10; i++) {
					RandomSource r = blockEntity.getLevel()
						.getRandom();
					double sX = cannonOffset.x * .01f;
					double sY = (cannonOffset.y + 1) * .01f;
					double sZ = cannonOffset.z * .01f;
					double rX = r.nextFloat() - sX * 40;
					double rY = r.nextFloat() - sY * 40;
					double rZ = r.nextFloat() - sZ * 40;
					blockEntity.getLevel()
						.addParticle(ParticleTypes.CLOUD, start.x + rX, start.y + rY, start.z + rZ, sX, sY, sZ);
				}
			}

		}
	}

	@Override
	public boolean shouldRenderOffScreen(SchematicannonBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 128;
	}

}
