package com.simibubi.create.content.logistics.trains.entity;

import java.util.Objects;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionEntityRenderer;

import com.simibubi.create.content.logistics.trains.track.AbstractBogeyTileEntity;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
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

			if (!Backend.canUseInstancing(entity.level) && !entity.getContraption()
				.isHiddenInPortal(bogeyPos)) {

				ms.pushPose();
				translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, partialTicks);

				int light = getBogeyLightCoords(entity, bogey, partialTicks);
				BlockEntity be = entity.getContraption().presentTileEntities.get(bogeyPos);

				bogey.type.render(null, bogey.isUpsideDown(), bogey.wheelAngle.getValue(partialTicks), ms, partialTicks, buffers, light,
					overlay, (AbstractBogeyTileEntity) be);

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
		TransformStack.cast(ms)
			.rotateY(viewYRot + 90)
			.rotateX(-viewXRot)
			.rotateY(180)
			.translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing)
			.rotateY(-180)
			.rotateX(viewXRot)
			.rotateY(-viewYRot - 90)
			.rotateY(bogey.yaw.getValue(partialTicks))
			.rotateX(bogey.pitch.getValue(partialTicks))
			.translate(0, .5f, 0)
			.rotateZ(selfUpsideDown ? 180 : 0)
			.translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
	}

	public static int getBogeyLightCoords(CarriageContraptionEntity entity, CarriageBogey bogey, float partialTicks) {

		var lightPos = new BlockPos(
			Objects.requireNonNullElseGet(bogey.getAnchorPosition(), () -> entity.getLightProbePosition(partialTicks)));

		return LightTexture.pack(entity.level.getBrightness(LightLayer.BLOCK, lightPos),
			entity.level.getBrightness(LightLayer.SKY, lightPos));
	}

}
