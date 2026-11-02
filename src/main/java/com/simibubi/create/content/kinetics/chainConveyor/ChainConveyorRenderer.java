package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.Camera;

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

import org.joml.Vector3f;

public class ChainConveyorRenderer extends KineticBlockEntityRenderer<ChainConveyorBlockEntity> {

	public static final ResourceLocation CHAIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/block/chain.png");
	public static final int MIP_DISTANCE = 48;
	private static final float LOD_EPSILON = 1e-6f;

	private record ChainRenderContext(ChainConveyorBlockEntity conveyor, PoseStack poseStack,
		MultiBufferSource buffer, float animation, int light, int overlay, boolean useLod) {
	}

	private record LodCut(float nearLength, float farLength, boolean startFarther) {
	}

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

	/**
	 * Cut the line segment based on depth.
	 * The pose matrix maps positions relative to the block entity into camera-relative render space,
	 * including any transforms applied by a parent renderer. In that space,
	 * dot(point, cameraForward) = MIP_DISTANCE is the plane separating near and far geometry.
	 * The output contains the lengths inside and outside LOD
	 * and whether the start point lies on the far side of the plane.
	 */
	private static LodCut calculateLodCut(PoseStack ms, BlockPos origin, ConnectionStats stats) {
		Vec3 blockOrigin = Vec3.atLowerCornerOf(origin);
		Vector3f renderedStart = stats.start().subtract(blockOrigin).toVector3f();
		Vector3f renderedEnd = stats.end().subtract(blockOrigin).toVector3f();
		Matrix4f transform = ms.last().pose();
		transform.transformPosition(renderedStart);
		transform.transformPosition(renderedEnd);

		Camera camera = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera;
		Vector3f forward = camera.getLookVector();
		float distStart = renderedStart.dot(forward) - MIP_DISTANCE;
		float distEnd = renderedEnd.dot(forward) - MIP_DISTANCE;
		float totalLength = stats.chainLength();

		if (distStart <= 0 && distEnd <= 0) // Both points are inside LOD
			return new LodCut(totalLength, 0, false);

		if (distStart >= 0 && distEnd >= 0) // Both points are outside LOD
			return new LodCut(0, totalLength, true);

		float denom = distStart - distEnd;
		float firstLength = totalLength * Mth.clamp(distStart / denom, 0, 1);
		if (distStart > 0)
			return new LodCut(totalLength - firstLength, firstLength, true);
		return new LodCut(firstLength, totalLength - firstLength, false);
	}

	private void renderChains(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		if (be.connectionStats == null)
			return;

		float time = AnimationTickHolder.getRenderTime(be.getLevel()) / (360f / Math.abs(be.getSpeed()));
		time %= 1;
		if (time < 0)
			time += 1;

		Level level = be.getLevel();
		Minecraft minecraft = Minecraft.getInstance();
		Camera camera = minecraft.getBlockEntityRenderDispatcher().camera;
		boolean useLod = minecraft.level == level;
		boolean renderRemote = useLod && shouldRender(be, camera.getPosition());
		ChainRenderContext context =
			new ChainRenderContext(be, ms, buffer, time - 0.5f, light, overlay, useLod);
		BlockPos tilePos = be.getBlockPos();
		int light1 = getLight(level, tilePos);

		for (BlockPos blockPos : be.connections) {
			ConnectionStats stats = be.connectionStats.get(blockPos);
			if (stats == null)
				continue;

			BlockPos targetPos = tilePos.offset(blockPos);
			BlockPos reverseConnection = blockPos.multiply(-1);
			ChainConveyorBlockEntity targetConveyor = null;
			if (level.getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity target)
				targetConveyor = target;
			int light2 = targetConveyor != null ? getLight(level, targetPos) : light1;

			renderGuard(context, stats);
			renderConnection(context, stats, light1, light2);

			// Render the "virtual" chain on the other side if the target is not rendered
			// So that we could see a pair of chains
			// Do not render this if we are in a virtual world
			if (!renderRemote)
				continue;
			boolean targetWillRender = targetConveyor != null && targetConveyor.connectionStats != null
				&& targetConveyor.connectionStats.containsKey(reverseConnection);
			if (targetWillRender
				&& camera.getPosition().closerThan(targetPos.getCenter(), minecraft.gameRenderer.getRenderDistance()))
				continue;

			ConnectionStats virtualStats = ChainConveyorBlockEntity.calculateConnectionStats(
				reverseConnection, targetPos, be.getSpeed() < 0
			);
			renderConnection(context, virtualStats, light1, light1);
		}
	}

	private static int getLight(Level level, BlockPos pos) {
		return LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos),
			level.getBrightness(LightLayer.SKY, pos));
	}

	private static void renderGuard(ChainRenderContext context, ConnectionStats stats) {
		ChainConveyorBlockEntity be = context.conveyor();
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		Vec3 diff = stats.end().subtract(stats.start());
		float yaw = Mth.RAD_TO_DEG * (float) Mth.atan2(diff.x, diff.z);
		SuperByteBuffer guard = CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, be.getBlockState());
		guard.center();
		guard.rotateYDegrees(yaw);
		guard.uncenter();
		guard.light(context.light())
			.overlay(context.overlay())
			.renderInto(context.poseStack(), context.buffer().getBuffer(RenderType.cutoutMipped()));
	}

	private static void renderConnection(ChainRenderContext context, ConnectionStats stats, int light1, int light2) {
		Vec3 start = stats.start();
		Vec3 end = stats.end();
		Vec3 diff = end.subtract(start);
		float yaw = Mth.RAD_TO_DEG * (float) Mth.atan2(diff.x, diff.z);
		float pitch = Mth.RAD_TO_DEG * (float) Mth.atan2(diff.y, diff.multiply(1, 0, 1).length());
		BlockPos conveyorPos = context.conveyor().getBlockPos();
		Vec3 startOffset = start.subtract(Vec3.atCenterOf(conveyorPos));

		PoseStack ms = context.poseStack();
		LodCut cut = context.useLod() ? calculateLodCut(ms, conveyorPos, stats) : null;
		ms.pushPose();
		var chain = TransformStack.of(ms);
		chain.center();
		chain.translate(startOffset);
		chain.rotateYDegrees(yaw);
		chain.rotateXDegrees(90 - pitch);
		chain.rotateYDegrees(45);
		chain.translate(0, 8 / 16f, 0);
		chain.uncenter();

		if (cut != null)
			renderChainWithLod(ms, context.buffer(), context.animation(), light1, light2, cut, chain);
		else
			renderChainSegment(ms, context.buffer(), context.animation(), 0, stats.chainLength(), light1, light2, false);
		ms.popPose();
	}

	private static void renderChainWithLod(PoseStack ms, MultiBufferSource buffer, float animation, int light1,
		int light2, LodCut cut, TransformStack chain) {
		float firstLength = cut.startFarther() ? cut.farLength() : cut.nearLength();
		float secondLength = cut.startFarther() ? cut.nearLength() : cut.farLength();

		if (firstLength > LOD_EPSILON)
			renderChainSegment(ms, buffer, animation, 0, firstLength, light1, light2, cut.startFarther());

		if (secondLength > LOD_EPSILON) {
			chain.translate(0, firstLength, 0);
			renderChainSegment(ms, buffer, animation, firstLength, secondLength, light1, light2,
				!cut.startFarther());
		}
	}

	public static void renderChain(PoseStack ms, MultiBufferSource buffer, float animation, float length, int light1,
		int light2, boolean far) {
		float minV = far ? 0 : animation;
		float maxV = far ? 1 / 16f : length + minV;
		renderChainGeometry(ms, buffer, length, light1, light2, far, minV, maxV);
	}

	private static void renderChainSegment(PoseStack ms, MultiBufferSource buffer, float animation, float start,
		float length, int light1, int light2, boolean far) {
		float maxV = far ? 0 : animation - start;
		float minV = far ? 1 / 16f : maxV - length;
		renderChainGeometry(ms, buffer, length, light1, light2, far, minV, maxV);
	}

	private static void renderChainGeometry(PoseStack ms, MultiBufferSource buffer, float length, int light1,
		int light2, boolean far, float minV, float maxV) {
		float radius = far ? 1f / 16f : 1.5f / 16f;
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
