package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public sealed class BogeyInstance {

	public final CarriageBogey bogey;
	private final ModelData[] shafts;

	protected BogeyInstance(CarriageBogey bogey, MaterialManager materialManager) {
		this.bogey = bogey;

		shafts = new ModelData[2];

		materialManager.defaultSolid()
			.material(Materials.TRANSFORMED)
			.getModel(AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, Direction.Axis.Z))
			.createInstances(shafts);

	}

	public void remove() {
		for (ModelData shaft : shafts)
			shaft.delete();
	}

	public void hiddenFrame() {
		beginFrame(0, null);
	}
	
	public void beginFrame(float wheelAngle, PoseStack ms) {
		if (ms == null) {
			for (int i : Iterate.zeroAndOne)
				shafts[i].setEmptyTransform();
			return;
		}

		for (int i : Iterate.zeroAndOne)
			shafts[i].setTransform(ms)
				.translate(-.5f, .25f, i * -1)
				.centre()
				.rotateZ(wheelAngle)
				.unCentre();
	}

	public void updateLight(BlockAndTintGetter world, CarriageContraptionEntity entity) {
		var lightPos = new BlockPos(getLightPos(entity));

		updateLight(world.getBrightness(LightLayer.BLOCK, lightPos), world.getBrightness(LightLayer.SKY, lightPos));
	}

	private Vec3 getLightPos(CarriageContraptionEntity entity) {
		if (bogey.getAnchorPosition() != null) {
			return bogey.getAnchorPosition();
		} else {
			return entity.getLightProbePosition(AnimationTickHolder.getPartialTicks());
		}
	}

	public void updateLight(int blockLight, int skyLight) {
		for (ModelData shaft : shafts) {
			shaft.setBlockLight(blockLight)
				.setSkyLight(skyLight);
		}
	}

	public static final class Frame extends BogeyInstance {

		private final ModelData frame;
		private final ModelData[] wheels;

		public Frame(CarriageBogey bogey, MaterialManager materialManager) {
			super(bogey, materialManager);

			frame = materialManager.defaultSolid()
				.material(Materials.TRANSFORMED)
				.getModel(AllPartialModels.BOGEY_FRAME)
				.createInstance();

			wheels = new ModelData[2];

			materialManager.defaultSolid()
				.material(Materials.TRANSFORMED)
				.getModel(AllPartialModels.SMALL_BOGEY_WHEELS)
				.createInstances(wheels);
		}

		@Override
		public void beginFrame(float wheelAngle, PoseStack ms) {
			super.beginFrame(wheelAngle, ms);

			if (ms == null) {
				frame.setEmptyTransform();
				for (int side : Iterate.positiveAndNegative)
					wheels[(side + 1) / 2].setEmptyTransform();
				return;
			}

			frame.setTransform(ms);

			for (int side : Iterate.positiveAndNegative) {
				wheels[(side + 1) / 2].setTransform(ms)
					.translate(0, 12 / 16f, side)
					.rotateX(wheelAngle);
			}
		}

		@Override
		public void updateLight(int blockLight, int skyLight) {
			super.updateLight(blockLight, skyLight);
			frame.setBlockLight(blockLight)
				.setSkyLight(skyLight);
			for (ModelData wheel : wheels)
				wheel.setBlockLight(blockLight)
					.setSkyLight(skyLight);
		}

		@Override
		public void remove() {
			super.remove();
			frame.delete();
			for (ModelData wheel : wheels)
				wheel.delete();
		}
	}

	public static final class Drive extends BogeyInstance {

		private final ModelData[] secondShaft;
		private final ModelData drive;
		private final ModelData piston;
		private final ModelData wheels;
		private final ModelData pin;

		public Drive(CarriageBogey bogey, MaterialManager materialManager) {
			super(bogey, materialManager);
			Material<ModelData> mat = materialManager.defaultSolid()
				.material(Materials.TRANSFORMED);

			secondShaft = new ModelData[2];

			mat.getModel(AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, Direction.Axis.X))
				.createInstances(secondShaft);

			drive = mat.getModel(AllPartialModels.BOGEY_DRIVE)
				.createInstance();
			piston = mat.getModel(AllPartialModels.BOGEY_PISTON)
				.createInstance();
			wheels = mat.getModel(AllPartialModels.LARGE_BOGEY_WHEELS)
				.createInstance();
			pin = mat.getModel(AllPartialModels.BOGEY_PIN)
				.createInstance();
		}

		@Override
		public void beginFrame(float wheelAngle, PoseStack ms) {
			super.beginFrame(wheelAngle, ms);

			if (ms == null) {
				for (int i : Iterate.zeroAndOne)
					secondShaft[i].setEmptyTransform();
				drive.setEmptyTransform();
				piston.setEmptyTransform();
				wheels.setEmptyTransform();
				pin.setEmptyTransform();
				return;
			}

			for (int i : Iterate.zeroAndOne)
				secondShaft[i].setTransform(ms)
					.translate(-.5f, .25f, .5f + i * -2)
					.centre()
					.rotateX(wheelAngle)
					.unCentre();

			drive.setTransform(ms);
			piston.setTransform(ms)
				.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)));

			wheels.setTransform(ms)
				.translate(0, 1, 0)
				.rotateX(wheelAngle);
			pin.setTransform(ms)
				.translate(0, 1, 0)
				.rotateX(wheelAngle)
				.translate(0, 1 / 4f, 0)
				.rotateX(-wheelAngle);
		}

		@Override
		public void updateLight(int blockLight, int skyLight) {
			super.updateLight(blockLight, skyLight);
			for (ModelData shaft : secondShaft)
				shaft.setBlockLight(blockLight)
					.setSkyLight(skyLight);
			drive.setBlockLight(blockLight)
				.setSkyLight(skyLight);
			piston.setBlockLight(blockLight)
				.setSkyLight(skyLight);
			wheels.setBlockLight(blockLight)
				.setSkyLight(skyLight);
			pin.setBlockLight(blockLight)
				.setSkyLight(skyLight);
		}

		@Override
		public void remove() {
			super.remove();
			for (ModelData shaft : secondShaft)
				shaft.delete();
			drive.delete();
			piston.delete();
			wheels.delete();
			pin.delete();
		}
	}
}
