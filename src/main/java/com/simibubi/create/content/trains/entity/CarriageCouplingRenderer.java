package com.simibubi.create.content.trains.entity;

import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CarriageCouplingRenderer {

	public static void renderAll(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
		Collection<Train> trains = CreateClient.RAILWAYS.trains.values();
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		BlockState air = Blocks.AIR.defaultBlockState();
		float partialTicks = AnimationTickHolder.getPartialTicks();
		Level level = Minecraft.getInstance().level;

		for (Train train : trains) {
			List<Carriage> carriages = train.carriages;
			for (int i = 0; i < carriages.size() - 1; i++) {
				Carriage carriage = carriages.get(i);
				CarriageContraptionEntity entity = carriage.getDimensional(level).entity.get();
				Carriage carriage2 = carriages.get(i + 1);
				CarriageContraptionEntity entity2 = carriage.getDimensional(level).entity.get();

				if (entity == null || entity2 == null)
					continue;

				CarriageBogey bogey1 = carriage.trailingBogey();
				CarriageBogey bogey2 = carriage2.leadingBogey();
				Vec3 anchor = bogey1.couplingAnchors.getSecond();
				Vec3 anchor2 = bogey2.couplingAnchors.getFirst();

				if (anchor == null || anchor2 == null)
					continue;
				if (!anchor.closerThan(camera, 64))
					continue;

				int lightCoords = getPackedLightCoords(entity, partialTicks);
				int lightCoords2 = getPackedLightCoords(entity2, partialTicks);

				double diffX = anchor2.x - anchor.x;
				double diffY = anchor2.y - anchor.y;
				double diffZ = anchor2.z - anchor.z;
				float yRot = AngleHelper.deg(Mth.atan2(diffZ, diffX)) + 90;
				float xRot = AngleHelper.deg(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));

				Vec3 position = entity.getPosition(partialTicks);
				Vec3 position2 = entity2.getPosition(partialTicks);

				ms.pushPose();

				{
					ms.pushPose();
					ms.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
					CachedBufferer.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air)
						.rotateYDegrees(-yRot)
						.rotateXDegrees(xRot)
						.light(lightCoords)
						.renderInto(ms, vb);

					float margin = 3 / 16f;
					double couplingDistance = train.carriageSpacing.get(i) - 2 * margin
						- bogey1.type.getConnectorAnchorOffset(bogey1.isUpsideDown()).z - bogey2.type.getConnectorAnchorOffset(bogey2.isUpsideDown()).z;
					int couplingSegments = (int) Math.round(couplingDistance * 4);
					double stretch = ((anchor2.distanceTo(anchor) - 2 * margin) * 4) / couplingSegments;
					for (int j = 0; j < couplingSegments; j++) {
						CachedBufferer.partial(AllPartialModels.TRAIN_COUPLING_CABLE, air)
							.rotateYDegrees(-yRot + 180)
							.rotateXDegrees(-xRot)
							.translate(0, 0, margin + 2 / 16f)
							.scale(1, 1, (float) stretch)
							.translate(0, 0, j / 4f)
							.light(lightCoords)
							.renderInto(ms, vb);
					}
					ms.popPose();
				}

				{
					ms.pushPose();
					Vec3 translation = position2.subtract(position)
						.add(anchor2)
						.subtract(camera);
					ms.translate(translation.x, translation.y, translation.z);
					CachedBufferer.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air)
						.rotateYDegrees(-yRot + 180)
						.rotateXDegrees(-xRot)
						.light(lightCoords2)
						.renderInto(ms, vb);
					ms.popPose();
				}

				ms.popPose();
			}
		}

	}

	public static int getPackedLightCoords(Entity pEntity, float pPartialTicks) {
		BlockPos blockpos = BlockPos.containing(pEntity.getLightProbePosition(pPartialTicks));
		return LightTexture.pack(getBlockLightLevel(pEntity, blockpos), getSkyLightLevel(pEntity, blockpos));
	}

	protected static int getSkyLightLevel(Entity pEntity, BlockPos pPos) {
		return pEntity.level().getBrightness(LightLayer.SKY, pPos);
	}

	protected static int getBlockLightLevel(Entity pEntity, BlockPos pPos) {
		return pEntity.isOnFire() ? 15 : pEntity.level().getBrightness(LightLayer.BLOCK, pPos);
	}

}
