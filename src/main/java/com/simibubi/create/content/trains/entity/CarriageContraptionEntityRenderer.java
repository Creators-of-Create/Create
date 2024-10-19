package com.simibubi.create.content.trains.entity;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class CarriageContraptionEntityRenderer extends ContraptionEntityRenderer<CarriageContraptionEntity> {

	public CarriageContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(CarriageContraptionEntity entity, Frustum clippingHelper, double cameraX,
		double cameraY, double cameraZ) {
		Carriage carriage = entity.getCarriage();
		if (carriage != null)
			for (CarriageBogey bogey : carriage.bogeys)
				if (bogey != null)
					bogey.couplingAnchors.replace(v -> null);
		return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
	}

	@Override
	public void render(CarriageContraptionEntity entity, float yaw, float partialTicks, PoseStack ms,
		MultiBufferSource buffers, int overlay) {
		if (!entity.validForRender || entity.firstPositionUpdate)
			return;

		super.render(entity, yaw, partialTicks, ms, buffers, overlay);

		Carriage carriage = entity.getCarriage();
		if (carriage == null)
			return;

		Vec3 position = entity.getPosition(partialTicks);

		float viewYRot = entity.getViewYRot(partialTicks);
		float viewXRot = entity.getViewXRot(partialTicks);
		int bogeySpacing = carriage.bogeySpacing;

		carriage.bogeys.forEach(bogey -> {
			if (bogey == null)
				return;

			BlockPos bogeyPos = bogey.isLeading ? BlockPos.ZERO
				: BlockPos.ZERO.relative(entity.getInitialOrientation()
					.getCounterClockWise(), bogeySpacing);

			if (!VisualizationManager.supportsVisualization(entity.level()) && !entity.getContraption()
				.isHiddenInPortal(bogeyPos)) {

				ms.pushPose();
				translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, partialTicks);

				int light = getBogeyLightCoords(entity, bogey, partialTicks);

				bogey.getStyle().render(bogey.getSize(), partialTicks, ms, buffers, light,
					overlay, bogey.wheelAngle.getValue(partialTicks), bogey.bogeyData, true);

				ms.popPose();
			}

			bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, partialTicks, bogey.isLeading);
			if (!carriage.isOnTwoBogeys())
				bogey.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, partialTicks, !bogey.isLeading);
		});
	}

	public static void translateBogey(PoseStack ms, CarriageBogey bogey, int bogeySpacing, float viewYRot,
		float viewXRot, float partialTicks) {
		boolean selfUpsideDown = bogey.isUpsideDown();
		boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
		TransformStack.of(ms)
			.rotateYDegrees(viewYRot + 90)
			.rotateXDegrees(-viewXRot)
			.rotateYDegrees(180)
			.translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing)
			.rotateYDegrees(-180)
			.rotateXDegrees(viewXRot)
			.rotateYDegrees(-viewYRot - 90)
			.rotateYDegrees(bogey.yaw.getValue(partialTicks))
			.rotateXDegrees(bogey.pitch.getValue(partialTicks))
			.translate(0, .5f, 0)
			.rotateZDegrees(selfUpsideDown ? 180 : 0)
			.translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
	}

	public static int getBogeyLightCoords(CarriageContraptionEntity entity, CarriageBogey bogey, float partialTicks) {
		var lightPos = BlockPos.containing(
			Objects.requireNonNullElseGet(bogey.getAnchorPosition(), () -> entity.getLightProbePosition(partialTicks)));

		return LightTexture.pack(entity.level().getBrightness(LightLayer.BLOCK, lightPos),
			entity.level().getBrightness(LightLayer.SKY, lightPos));
	}

}
