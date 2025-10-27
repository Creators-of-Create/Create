package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.List;
import java.util.Map.Entry;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage.ChainConveyorPackagePhysicsData;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.render.RenderTypes;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChainConveyorRenderer extends KineticBlockEntityRenderer<ChainConveyorBlockEntity> {

	public static final ResourceLocation CHAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/chain.png");
	public static final int MIP_DISTANCE = 48;

	public ChainConveyorRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ChainConveyorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		BlockPos pos = be.getBlockPos();

		renderChains(be, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_WHEEL, be.getBlockState())
			.light(light)
			.overlay(overlay)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		for (ChainConveyorPackage box : be.loopingPackages)
			renderBox(be, ms, buffer, overlay, pos, box, partialTicks);
		for (Entry<BlockPos, List<ChainConveyorPackage>> entry : be.travellingPackages.entrySet())
			for (ChainConveyorPackage box : entry.getValue())
				renderBox(be, ms, buffer, overlay, pos, box, partialTicks);
	}

	private void renderBox(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int overlay,
		BlockPos pos, ChainConveyorPackage box, float partialTicks) {
		if (box.worldPosition == null)
			return;
		if (box.item == null || box.item.isEmpty())
			return;

		ChainConveyorPackagePhysicsData physicsData = box.physicsData(be.getLevel());
		if (physicsData.prevPos == null)
			return;

		Vec3 position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
		Vec3 targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
		float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
		Vec3 offset =
			new Vec3(targetPosition.x - pos.getX(), targetPosition.y - pos.getY(), targetPosition.z - pos.getZ());

		BlockPos containingPos = BlockPos.containing(position);
		Level level = be.getLevel();
		BlockState blockState = be.getBlockState();
		int light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, containingPos),
			level.getBrightness(LightLayer.SKY, containingPos));

		if (physicsData.modelKey == null) {
			ResourceLocation key = BuiltInRegistries.ITEM.getKey(box.item.getItem());
			if (key == BuiltInRegistries.ITEM.getDefaultKey())
				return;
			physicsData.modelKey = key;
		}

		SuperByteBuffer rigBuffer =
			CachedBuffers.partial(AllPartialModels.PACKAGE_RIGGING.get(physicsData.modelKey), blockState);
		SuperByteBuffer boxBuffer =
			CachedBuffers.partial(AllPartialModels.PACKAGES.get(physicsData.modelKey), blockState);

		Vec3 dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0)
			.subtract(position), -yaw, Axis.Y);
		float zRot = Mth.wrapDegrees((float) Mth.atan2(-dangleDiff.x, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
		float xRot = Mth.wrapDegrees((float) Mth.atan2(dangleDiff.z, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
		zRot = Mth.clamp(zRot, -25, 25);
		xRot = Mth.clamp(xRot, -25, 25);

		for (SuperByteBuffer buf : new SuperByteBuffer[] { rigBuffer, boxBuffer }) {
			buf.translate(offset);
			buf.translate(0, 10 / 16f, 0);
			buf.rotateYDegrees(yaw);

			buf.rotateZDegrees(zRot);
			buf.rotateXDegrees(xRot);

			if (physicsData.flipped && buf == rigBuffer)
				buf.rotateYDegrees(180);

			buf.uncenter();
			buf.translate(0, -PackageItem.getHookDistance(box.item) + 7 / 16f, 0);

			buf.light(light)
				.overlay(overlay)
				.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
		}
	}

	private void renderChains(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		float time = AnimationTickHolder.getRenderTime(be.getLevel()) / (360f / Math.abs(be.getSpeed()));
		time %= 1;
		if (time < 0)
			time += 1;

		float animation = time - 0.5f;

		for (BlockPos blockPos : be.connections) {
			ConnectionStats stats = be.connectionStats.get(blockPos);
			if (stats == null)
				continue;

			Vec3 diff = stats.end()
				.subtract(stats.start());
			double yaw = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
			double pitch = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1)
				.length());

			Level level = be.getLevel();
			BlockPos tilePos = be.getBlockPos();
			Vec3 startOffset = stats.start()
				.subtract(Vec3.atCenterOf(tilePos));

			if (!VisualizationManager.supportsVisualization(be.getLevel())) {
				SuperByteBuffer guard =
					CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, be.getBlockState());
				guard.center();
				guard.rotateYDegrees((float) yaw);

				guard.uncenter();
				guard.light(light)
					.overlay(overlay)
					.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
			}

			ms.pushPose();
			var chain = TransformStack.of(ms);
			chain.center();
			chain.translate(startOffset);
			chain.rotateYDegrees((float) yaw);
			chain.rotateXDegrees(90 - (float) pitch);
			chain.rotateYDegrees(45);
			chain.translate(0, 8 / 16f, 0);
			chain.uncenter();

			int light1 = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, tilePos),
				level.getBrightness(LightLayer.SKY, tilePos));
			int light2 = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, tilePos.offset(blockPos)),
				level.getBrightness(LightLayer.SKY, tilePos.offset(blockPos)));

			boolean far = Minecraft.getInstance().level == be.getLevel() && !Minecraft.getInstance()
				.getBlockEntityRenderDispatcher().camera.getPosition()
					.closerThan(Vec3.atCenterOf(tilePos)
						.add(blockPos.getX() / 2f, blockPos.getY() / 2f, blockPos.getZ() / 2f), MIP_DISTANCE);

			renderChain(ms, buffer, animation, stats.chainLength(), light1, light2, far);

			ms.popPose();
		}
	}

	public static void renderChain(PoseStack ms, MultiBufferSource buffer, float animation, float length, int light1,
		int light2, boolean far) {
		float radius = far ? 1f / 16f : 1.5f / 16f;
		float minV = far ? 0 : animation;
		float maxV = far ? 1 / 16f : length + minV;
		float minU = far ? 3 / 16f : 0;
		float maxU = far ? 4 / 16f : 3 / 16f;

		ms.pushPose();
		ms.translate(0.5D, 0.0D, 0.5D);

		VertexConsumer vc = buffer.getBuffer(RenderTypes.chain(CHAIN_LOCATION));
		renderPart(ms, vc, length, 0.0F, radius, radius, 0.0F, -radius, 0.0F, 0.0F, -radius, minU, maxU, minV, maxV,
			light1, light2, far);

		ms.popPose();
	}

	private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer, float pMaxY, float pX0, float pZ0,
		float pX1, float pZ1, float pX2, float pZ2, float pX3, float pZ3, float pMinU, float pMaxU, float pMinV,
		float pMaxV, int light1, int light2, boolean far) {
		PoseStack.Pose posestack$pose = pPoseStack.last();
		Matrix4f matrix4f = posestack$pose.pose();

		float uO = far ? 0f : 3 / 16f;
		renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX0, pZ0, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV, light1,
			light2);
		renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX3, pZ3, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV, light1,
			light2);
		renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX1, pZ1, pX2, pZ2, pMinU + uO, pMaxU + uO, pMinV, pMaxV,
			light1, light2);
		renderQuad(matrix4f, posestack$pose, pConsumer, 0, pMaxY, pX2, pZ2, pX1, pZ1, pMinU + uO, pMaxU + uO, pMinV, pMaxV,
			light1, light2);
	}

	private static void renderQuad(Matrix4f pPose, PoseStack.Pose pNormal, VertexConsumer pConsumer, float pMinY, float pMaxY,
		float pMinX, float pMinZ, float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV,
		int light1, int light2) {
		addVertex(pPose, pNormal, pConsumer, pMaxY, pMinX, pMinZ, pMaxU, pMinV, light2);
		addVertex(pPose, pNormal, pConsumer, pMinY, pMinX, pMinZ, pMaxU, pMaxV, light1);
		addVertex(pPose, pNormal, pConsumer, pMinY, pMaxX, pMaxZ, pMinU, pMaxV, light1);
		addVertex(pPose, pNormal, pConsumer, pMaxY, pMaxX, pMaxZ, pMinU, pMinV, light2);
	}

	private static void addVertex(Matrix4f pPose, PoseStack.Pose pNormal, VertexConsumer pConsumer, float pY, float pX,
		float pZ, float pU, float pV, int light) {
		pConsumer.addVertex(pPose, pX, pY, pZ)
			.setColor(1.0f, 1.0f, 1.0f, 1.0f)
			.setUv(pU, pV)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(pNormal, 0.0F, 1.0F, 0.0F);
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	@Override
	public boolean shouldRenderOffScreen(ChainConveyorBlockEntity be) {
		return true;
	}

	@Override
	protected SuperByteBuffer getRotatedModel(ChainConveyorBlockEntity be, BlockState state) {
		return CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT, state);
	}

	@Override
	protected RenderType getRenderType(ChainConveyorBlockEntity be, BlockState state) {
		return RenderType.cutoutMipped();
	}

}
