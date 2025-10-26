package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.ChunkAccess;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import org.joml.FrustumIntersection;
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
	public static final int MIP_DISTANCE_SQR = 48 * 48;

	public ChainConveyorRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ChainConveyorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		BlockPos pos = be.getBlockPos();

		FrustumIntersection frustum = null;
		Vec3 camPos = null;
		if (Minecraft.getInstance().level == be.getLevel()) {
			frustum = getFrustumIntersection();
			camPos = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera.getPosition();
		}
		boolean renderCentre = frustum == null || frustum.testAab(pos.getX() - 2 - (float) camPos.x, pos.getY() - (float) camPos.y, pos.getZ() - 2 - (float) camPos.z, pos.getX() + 2 - (float) camPos.x, pos.getY() + 1 - (float) camPos.y, pos.getZ() + 2 - (float) camPos.z);
		renderChains(be, ms, buffer, light, overlay, frustum, camPos, renderCentre);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		if (renderCentre)
			CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_WHEEL, be.getBlockState())
				.light(light)
				.overlay(overlay)
				.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));

		for (ChainConveyorPackage box : be.loopingPackages)
			renderBox(be, ms, buffer, overlay, pos, box, partialTicks, frustum, camPos);
		for (Entry<BlockPos, List<ChainConveyorPackage>> entry : be.travellingPackages.entrySet())
			for (ChainConveyorPackage box : entry.getValue())
				renderBox(be, ms, buffer, overlay, pos, box, partialTicks, frustum, camPos);
	}

	private void renderBox(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int overlay,
		BlockPos pos, ChainConveyorPackage box, float partialTicks, FrustumIntersection frustum, Vec3 camPos) {
		if (box.worldPosition == null)
			return;
		if (box.item == null || box.item.isEmpty())
			return;

		ChainConveyorPackagePhysicsData physicsData = box.physicsData(be.getLevel());
		if (physicsData.prevPos == null)
			return;

		Vec3 position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
		Vec3 targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
		if (frustum != null && !frustum.testSphere(
			(float) (targetPosition.x - camPos.x),
			(float) (targetPosition.y - camPos.y),
			(float) (targetPosition.z - camPos.z), 1))
			return;

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
	 * Calculate the intersection points between a line segment and a circle centered at cameraPos with radius LODDistance.
	 * The intersections array is used to store up to 2 intersection points.
	 * Returns the number of intersection points (0, 1, or 2).
	 */
	private static int calculateLineCircleIntersection(Vec3 start, Vec3 end, Vec3 cameraPos, Vec3[] intersections) {
		Vec3 ab = end.subtract(start);
		Vec3 ac = start.subtract(cameraPos);
		float a = (float) ab.lengthSqr();
		float b = 2 * (float) ac.dot(ab);
		float c = (float) ac.lengthSqr() - MIP_DISTANCE_SQR;
		float discriminant = b * b - 4 * a * c;

		if (discriminant < 0) {
			return 0; // No intersection
		}

		float sqrtDisc = Mth.sqrt(discriminant);
		float t1 = (-b - sqrtDisc) / (2 * a);
		float t2 = (-b + sqrtDisc) / (2 * a);
		int count = 0;
		if (t1 >= 0 && t1 <= 1) {
			intersections[count++] = start.add(ab.scale(t1));
		}
		// Avoid duplicate calculations (when t1 and t2 are almost equal)
		if (t2 >= 0 && t2 <= 1 && Math.abs(t2 - t1) > 1e-6f) {
			intersections[count++] = start.add(ab.scale(t2));
		}
		return count;
	}

	/**
	 * Cut the line segment based on the intersection points with the circle centered at the camera position.
	 * The output Vector3f contains:
	 * x: The distance from the start of the line segment to the intersection point (outside the LOD);
	 * y: The length of the part of the line segment LOD0;
	 * z: The distance from the intersection point to the end of the line segment (outside the LOD).
	 */
	public static Vector3f calculateLODCut(Vec3 start, Vec3 end, Vec3 cameraPos) {
		Vec3[] intersections = new Vec3[2];
		int intersectionCount = calculateLineCircleIntersection(start, end, cameraPos, intersections);
		float totalLength = (float) start.distanceTo(end);
		float x = 0, y = 0, z = 0;

		if (intersectionCount == 0) {
			// No intersection: Determine if the line segment is entirely inside or outside the circle
			if (start.distanceToSqr(cameraPos) < MIP_DISTANCE_SQR && end.distanceToSqr(cameraPos) < MIP_DISTANCE_SQR) {
				// Both ends are inside the circle
				y = totalLength;
			} else {
				// The line segment is entirely outside the circle
				x = totalLength;
			}
		} else if (intersectionCount == 1) {
			// Only one intersection point, determine which end is inside the circle
			// one end must be inside and the other outside
			boolean endInside = end.distanceToSqr(cameraPos) < MIP_DISTANCE_SQR;
			if (endInside) {
				x = (float) start.distanceTo(intersections[0]);
				y = (float) intersections[0].distanceTo(end);
				z = 0;
			} else {
				x = 0;
				y = (float) start.distanceTo(intersections[0]);
				z = (float) intersections[0].distanceTo(end);
			}
		} else if (intersectionCount == 2) {
			x = (float) start.distanceTo(intersections[0]);
			y = (float) intersections[0].distanceTo(intersections[1]);
			z = (float) end.distanceTo(intersections[1]);
		}

		return new Vector3f(x, y, z);
	}

	private void renderChains(ChainConveyorBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay, FrustumIntersection frustum, Vec3 camPos, boolean renderCentre) {
		if (frustum != null) {
			float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
			if (camPos.distanceToSqr(be.getBlockPos().getCenter()) > renderDistance * renderDistance)
				return;
		}
		float time = AnimationTickHolder.getRenderTime(be.getLevel()) / (360f / Math.abs(be.getSpeed()));
		time %= 1;
		if (time < 0)
			time += 1;

		float animation = time - 0.5f;

		for (BlockPos blockPos : be.connections) {
			ConnectionStats stats = be.connectionStats.get(blockPos);
			if (stats == null)
				continue;

			Level level = be.getLevel();
			BlockPos tilePos = be.getBlockPos();
			BlockPos targetPos = tilePos.offset(blockPos);

			Vec3 start = stats.start();
			Vec3 end = stats.end();

			Vec3 diff = end
				.subtract(start);
			double yaw = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
			if (!VisualizationManager.supportsVisualization(be.getLevel()) && renderCentre) {
				SuperByteBuffer guard =
					CachedBuffers.partial(AllPartialModels.CHAIN_CONVEYOR_GUARD, be.getBlockState());
				guard.center();
				guard.rotateYDegrees((float) yaw);

				guard.uncenter();
				guard.light(light)
					.overlay(overlay)
					.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
			}
			if (frustum == null || frustum.testLineSegment((float) (start.x - camPos.x), (float) (start.y - camPos.y), (float) (start.z - camPos.z),
				(float) (end.x - camPos.x), (float) (end.y - camPos.y), (float) (end.z - camPos.z))) {
				double pitch = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1)
					.length());

				int light1 = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, tilePos),
					level.getBrightness(LightLayer.SKY, tilePos));
				int light2 = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, targetPos),
					level.getBrightness(LightLayer.SKY, targetPos));

				Vec3 startOffset = start.subtract(Vec3.atCenterOf(tilePos));

				ms.pushPose();
				var chain = TransformStack.of(ms);
				chain.center();
				chain.translate(startOffset);
				chain.rotateYDegrees((float) yaw);
				chain.rotateXDegrees(90 - (float) pitch);
				chain.rotateYDegrees(45);
				chain.translate(0, 8 / 16f, 0);
				chain.uncenter();

				if (frustum != null) {
					renderChainWithLod(ms, buffer, animation, light1, light2, camPos, start, end, chain);
				} else {
					renderChain(ms, buffer, animation, 0, stats.chainLength(), light1, light2, false);
				}
				ms.popPose();
			}

			if (frustum == null)
				continue;

			float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
			if (camPos.distanceToSqr(targetPos.getCenter()) <= renderDistance * renderDistance)
				continue;

			boolean reversed = be.getSpeed() < 0;
			ConnectionStats virtualStats = ChainConveyorBlockEntity.calculateConnectionStats(
				blockPos.multiply(-1),
				targetPos,
				reversed
			);

			start = virtualStats.start();
			end = virtualStats.end();

			if (frustum.testLineSegment((float) (start.x - camPos.x), (float) (start.y - camPos.y), (float) (start.z - camPos.z),
				(float) (end.x - camPos.x), (float) (end.y - camPos.y), (float) (end.z - camPos.z))) {
				diff = end.subtract(start);
				yaw = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);
				double pitch = (float) Mth.RAD_TO_DEG * Mth.atan2(diff.y, diff.multiply(1, 0, 1).length());
				Vec3 startOffset = start.subtract(Vec3.atCenterOf(tilePos));

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

				renderChainWithLod(ms, buffer, animation, light1, light1, camPos, start, end, chain);
				ms.popPose();
			}

		}
	}

	public static void renderChainWithLod(PoseStack ms, MultiBufferSource buffer, float animation, int light1,
		int light2, Vec3 camPos, Vec3 chainStart, Vec3 chainEnd, TransformStack chain) {
		Vector3f length = calculateLODCut(chainStart, chainEnd, camPos);
		if (length.x > 1e-6f) {
			renderChain(ms, buffer, animation, 0, length.x, light1, light2, true);
		}

		if (length.y > 1e-6f) {
			chain.translate(0, length.x, 0);
			renderChain(ms, buffer, animation, length.x, length.y, light1, light2, false);
		}

		if (length.z > 1e-6f) {
			chain.translate(0, length.y, 0);
			renderChain(ms, buffer, animation, 0, length.z, light1, light2, true);
		}
	}

	public static void renderChain(PoseStack ms, MultiBufferSource buffer, float animation, float start, float length, int light1,
		int light2, boolean far) {
		float radius = far ? 1f / 16f : 1.5f / 16f;
		float maxV = far ? 0 : animation - start;
		float minV = far ? 1 / 16f : maxV - length;
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
