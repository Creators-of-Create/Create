package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class CarriageContraptionInstance extends EntityInstance<CarriageContraptionEntity> implements DynamicInstance {

	private final PoseStack ms = new PoseStack();

	private Carriage carriage;
	private Couple<BogeyInstance> bogeys;

	public CarriageContraptionInstance(MaterialManager materialManager, CarriageContraptionEntity entity) {
		super(materialManager, entity);
	}

	@Override
	public void init() {
		carriage = entity.getCarriage();

		if (carriage == null) return;

		bogeys = carriage.bogeys.mapNotNullWithParam(CarriageBogey::createInstance, materialManager);
		updateLight();
	}

	@Override
	public void beginFrame() {
		if (bogeys == null) {
			init();
			return;
		}

		float partialTicks = AnimationTickHolder.getPartialTicks();

		float viewYRot = entity.getViewYRot(partialTicks);
		float viewXRot = entity.getViewXRot(partialTicks);
		int bogeySpacing = carriage.bogeySpacing;

		ms.pushPose();

		TransformStack.cast(ms)
				.translate(getInstancePosition(partialTicks))
				.translate(0, -1.5 - 1 / 128f, 0);;

		for (BogeyInstance instance : bogeys) {
			if (instance != null) {
				ms.pushPose();
				CarriageBogey bogey = instance.bogey;

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
						.translate(0, .5f, 0);

				instance.beginFrame(bogey.wheelAngle.getValue(partialTicks), ms);
				ms.popPose();
			}
		}

		ms.popPose();
	}

	@Override
	public void updateLight() {
		if (bogeys == null) return;

		var pos = new BlockPos(entity.getLightProbePosition(AnimationTickHolder.getPartialTicks()));
		int block = world.getBrightness(LightLayer.BLOCK, pos);
		int sky = world.getBrightness(LightLayer.SKY, pos);

		bogeys.forEach(instance -> {
			if (instance != null)
				instance.updateLight(block, sky);
		});
	}

	@Override
	public void remove() {
		bogeys.forEach(BogeyInstance::remove);
	}


}
