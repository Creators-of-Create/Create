package com.simibubi.create.foundation.ponder.element;

import java.util.function.Supplier;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Parrot.Variant;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ParrotElement extends AnimatedSceneElement {

	private Vec3 location;
	private Parrot entity;
	private ParrotPose pose;
	private boolean deferConductor = false;
	private Supplier<? extends ParrotPose> initialPose;

	public static ParrotElement create(Vec3 location, Supplier<? extends ParrotPose> pose) {
		return new ParrotElement(location, pose);
	}

	protected ParrotElement(Vec3 location, Supplier<? extends ParrotPose> pose) {
		this.location = location;
		initialPose = pose;
		setPose(initialPose.get());
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		setPose(initialPose.get());
		entity.setPosRaw(0, 0, 0);
		entity.xo = 0;
		entity.yo = 0;
		entity.zo = 0;
		entity.xOld = 0;
		entity.yOld = 0;
		entity.zOld = 0;
		entity.setXRot(entity.xRotO = 0);
		entity.setYRot(entity.yRotO = 180);
		entity.getPersistentData()
			.remove("TrainHat");
		deferConductor = false;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (entity == null) {
			entity = pose.create(scene.getWorld());
			entity.setYRot(entity.yRotO = 180);
			if (deferConductor)
				setConductor(deferConductor);
			deferConductor = false;
		}

		entity.tickCount++;
		entity.yHeadRotO = entity.yHeadRot;
		entity.oFlapSpeed = entity.flapSpeed;
		entity.oFlap = entity.flap;
		entity.setOnGround(true);

		entity.xo = entity.getX();
		entity.yo = entity.getY();
		entity.zo = entity.getZ();
		entity.yRotO = entity.getYRot();
		entity.xRotO = entity.getXRot();

		pose.tick(scene, entity, location);

		entity.xOld = entity.getX();
		entity.yOld = entity.getY();
		entity.zOld = entity.getZ();
	}

	public void setPositionOffset(Vec3 position, boolean immediate) {
		if (entity == null)
			return;
		entity.setPos(position.x, position.y, position.z);
		if (!immediate)
			return;
		entity.xo = position.x;
		entity.yo = position.y;
		entity.zo = position.z;
	}

	public void setRotation(Vec3 eulers, boolean immediate) {
		if (entity == null)
			return;
		entity.setXRot((float) eulers.x);
		entity.setYRot((float) eulers.y);
		if (!immediate)
			return;
		entity.xRotO = entity.getXRot();
		entity.yRotO = entity.getYRot();
	}

	public void setConductor(boolean isConductor) {
		if (entity == null) {
			deferConductor = isConductor;
			return;
		}
		CompoundTag data = entity.getPersistentData();
		if (isConductor)
			data.putBoolean("TrainHat", true);
		else
			data.remove("TrainHat");
	}

	public Vec3 getPositionOffset() {
		return entity != null ? entity.position() : Vec3.ZERO;
	}

	public Vec3 getRotation() {
		return entity != null ? new Vec3(entity.getXRot(), entity.getYRot(), 0) : Vec3.ZERO;
	}

	@Override
	protected void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
		EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance()
			.getEntityRenderDispatcher();

		if (entity == null) {
			entity = pose.create(world);
			entity.setYRot(entity.yRotO = 180);
		}

		ms.pushPose();
		ms.translate(location.x, location.y, location.z);
		ms.translate(Mth.lerp(pt, entity.xo, entity.getX()), Mth.lerp(pt, entity.yo, entity.getY()),
			Mth.lerp(pt, entity.zo, entity.getZ()));

		TransformStack.of(ms)
			.rotateYDegrees(AngleHelper.angleLerp(pt, entity.yRotO, entity.getYRot()));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.popPose();
	}

	public void setPose(ParrotPose pose) {
		this.pose = pose;
	}

	public static abstract class ParrotPose {

		abstract void tick(PonderScene scene, Parrot entity, Vec3 location);

		Parrot create(PonderWorld world) {
			Parrot entity = new Parrot(EntityType.PARROT, world);
			Variant[] variants = Parrot.Variant.values();
			Parrot.Variant variant = variants[Create.RANDOM.nextInt(variants.length)];
			entity.setVariant(variant == Variant.BLUE ? Variant.RED_BLUE : variant); // blue parrots are difficult to see
			return entity;
		}

	}

	public static class DancePose extends ParrotPose {

		@Override
		Parrot create(PonderWorld world) {
			Parrot entity = super.create(world);
			entity.setRecordPlayingNearby(BlockPos.ZERO, true);
			return entity;
		}

		@Override
		void tick(PonderScene scene, Parrot entity, Vec3 location) {
			entity.yRotO = entity.getYRot();
			entity.setYRot(entity.getYRot() - 2);
		}

	}

	public static class FlappyPose extends ParrotPose {

		@Override
		void tick(PonderScene scene, Parrot entity, Vec3 location) {
			double length = entity.position()
				.subtract(entity.xOld, entity.yOld, entity.zOld)
				.length();
			entity.setOnGround(false);
			double phase = Math.min(length * 15, 8);
			float f = (float) ((PonderUI.ponderTicks % 100) * phase);
			entity.flapSpeed = Mth.sin(f) + 1;
			if (length == 0)
				entity.flapSpeed = 0;
		}

	}

	public static class SpinOnComponentPose extends ParrotPose {

		private BlockPos componentPos;

		public SpinOnComponentPose(BlockPos componentPos) {
			this.componentPos = componentPos;
		}

		@Override
		void tick(PonderScene scene, Parrot entity, Vec3 location) {
			BlockEntity blockEntity = scene.getWorld()
				.getBlockEntity(componentPos);
			if (!(blockEntity instanceof KineticBlockEntity))
				return;
			float rpm = ((KineticBlockEntity) blockEntity).getSpeed();
			entity.yRotO = entity.getYRot();
			entity.setYRot(entity.getYRot() + (rpm * .3f));
		}

	}

	public static abstract class FaceVecPose extends ParrotPose {

		@Override
		void tick(PonderScene scene, Parrot entity, Vec3 location) {
			Vec3 p_200602_2_ = getFacedVec(scene);
			Vec3 Vector3d = location.add(entity.getEyePosition(0));
			double d0 = p_200602_2_.x - Vector3d.x;
			double d1 = p_200602_2_.y - Vector3d.y;
			double d2 = p_200602_2_.z - Vector3d.z;
			double d3 = Mth.sqrt((float) (d0 * d0 + d2 * d2));
			float targetPitch = Mth.wrapDegrees((float) -(Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
			float targetYaw = Mth.wrapDegrees((float) -(Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) + 90);

			entity.setXRot(AngleHelper.angleLerp(.4f, entity.getXRot(), targetPitch));
			entity.setYRot(AngleHelper.angleLerp(.4f, entity.getYRot(), targetYaw));
		}

		protected abstract Vec3 getFacedVec(PonderScene scene);

	}

	public static class FacePointOfInterestPose extends FaceVecPose {

		@Override
		protected Vec3 getFacedVec(PonderScene scene) {
			return scene.getPointOfInterest();
		}

	}

	public static class FaceCursorPose extends FaceVecPose {

		@Override
		protected Vec3 getFacedVec(PonderScene scene) {
			Minecraft minecraft = Minecraft.getInstance();
			Window w = minecraft.getWindow();
			double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
			double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
			return scene.getTransform()
				.screenToScene(mouseX, mouseY, 300, 0);
		}

	}

}
