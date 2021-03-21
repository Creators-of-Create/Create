package com.simibubi.create.foundation.ponder.elements;

import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ParrotElement extends AnimatedSceneElement {

	private Vec3d location;
	private ParrotEntity entity;
	private ParrotPose pose;
	private Supplier<? extends ParrotPose> initialPose;

	public static ParrotElement create(Vec3d location, Supplier<? extends ParrotPose> pose) {
		return new ParrotElement(location, pose);
	}

	protected ParrotElement(Vec3d location, Supplier<? extends ParrotPose> pose) {
		this.location = location;
		initialPose = pose;
		setPose(initialPose.get());
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		setPose(initialPose.get());
		entity.setPos(0, 0, 0);
		entity.prevPosX = 0;
		entity.prevPosY = 0;
		entity.prevPosZ = 0;
		entity.lastTickPosX = 0;
		entity.lastTickPosY = 0;
		entity.lastTickPosZ = 0;
		entity.prevRotationPitch = entity.rotationPitch = 0;
		entity.prevRotationYaw = entity.rotationYaw = 180;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (entity == null) {
			entity = pose.create(scene.getWorld());
			entity.prevRotationYaw = entity.rotationYaw = 180;
		}

		entity.ticksExisted++;
		entity.prevRotationYawHead = entity.rotationYawHead;
		entity.oFlapSpeed = entity.flapSpeed;
		entity.oFlap = entity.flap;
		entity.onGround = true;

		entity.prevPosX = entity.getX();
		entity.prevPosY = entity.getY();
		entity.prevPosZ = entity.getZ();
		entity.prevRotationYaw = entity.rotationYaw;
		entity.prevRotationPitch = entity.rotationPitch;

		pose.tick(scene, entity, location);

		entity.lastTickPosX = entity.getX();
		entity.lastTickPosY = entity.getY();
		entity.lastTickPosZ = entity.getZ();
	}

	public void setPositionOffset(Vec3d position, boolean immediate) {
		if (entity == null)
			return;
		entity.setPosition(position.x, position.y, position.z);
		if (!immediate)
			return;
		entity.prevPosX = position.x;
		entity.prevPosY = position.y;
		entity.prevPosZ = position.z;
	}

	public void setRotation(Vec3d eulers, boolean immediate) {
		if (entity == null)
			return;
		entity.rotationPitch = (float) eulers.x;
		entity.rotationYaw = (float) eulers.y;
		if (!immediate)
			return;
		entity.prevRotationPitch = entity.rotationPitch;
		entity.prevRotationYaw = entity.rotationYaw;
	}

	public Vec3d getPositionOffset() {
		return entity != null ? entity.getPositionVec() : Vec3d.ZERO;
	}

	public Vec3d getRotation() {
		return entity != null ? new Vec3d(entity.rotationPitch, entity.rotationYaw, 0) : Vec3d.ZERO;
	}

	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getRenderManager();

		if (entity == null) {
			entity = pose.create(world);
			entity.prevRotationYaw = entity.rotationYaw = 180;
		}

		ms.push();
		ms.translate(location.x, location.y, location.z);
		ms.translate(MathHelper.lerp(pt, entity.prevPosX, entity.getX()),
			MathHelper.lerp(pt, entity.prevPosY, entity.getY()), MathHelper.lerp(pt, entity.prevPosZ, entity.getZ()));

		MatrixStacker.of(ms)
			.rotateY(AngleHelper.angleLerp(pt, entity.prevRotationYaw, entity.rotationYaw));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.pop();
	}

	public void setPose(ParrotPose pose) {
		this.pose = pose;
	}

	public static abstract class ParrotPose {

		abstract void tick(PonderScene scene, ParrotEntity entity, Vec3d location);

		ParrotEntity create(PonderWorld world) {
			ParrotEntity entity = new ParrotEntity(EntityType.PARROT, world);
			int nextInt = Create.random.nextInt(5);
			entity.setVariant(nextInt == 1 ? 0 : nextInt); // blue parrots are kinda hard to see
			return entity;
		}

	}

	public static class DancePose extends ParrotPose {

		@Override
		ParrotEntity create(PonderWorld world) {
			ParrotEntity entity = super.create(world);
			entity.setPartying(BlockPos.ZERO, true);
			return entity;
		}

		@Override
		void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
			entity.prevRotationYaw = entity.rotationYaw;
			entity.rotationYaw -= 2;
		}

	}

	public static class FlappyPose extends ParrotPose {

		@Override
		void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
			double length = entity.getPositionVec()
				.subtract(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)
				.length();
			entity.onGround = false;
			double phase = Math.min(length * 15, 8);
			float f = (float) ((PonderUI.ponderTicks % 100) * phase);
			entity.flapSpeed = MathHelper.sin(f) + 1;
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
		void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
			TileEntity tileEntity = scene.getWorld()
				.getTileEntity(componentPos);
			if (!(tileEntity instanceof KineticTileEntity))
				return;
			float rpm = ((KineticTileEntity) tileEntity).getSpeed();
			entity.prevRotationYaw = entity.rotationYaw;
			entity.rotationYaw += (rpm * .3f);
		}

	}

	public static abstract class FaceVecPose extends ParrotPose {

		@Override
		void tick(PonderScene scene, ParrotEntity entity, Vec3d location) {
			Vec3d p_200602_2_ = getFacedVec(scene);
			Vec3d vec3d = location.add(entity.getEyePosition(0));
			double d0 = p_200602_2_.x - vec3d.x;
			double d1 = p_200602_2_.y - vec3d.y;
			double d2 = p_200602_2_.z - vec3d.z;
			double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
			float targetPitch =
				MathHelper.wrapDegrees((float) -(MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
			float targetYaw =
				MathHelper.wrapDegrees((float) -(MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) + 90);

			entity.rotationPitch = AngleHelper.angleLerp(.4f, entity.rotationPitch, targetPitch);
			entity.rotationYaw = AngleHelper.angleLerp(.4f, entity.rotationYaw, targetYaw);
		}

		protected abstract Vec3d getFacedVec(PonderScene scene);

	}

	public static class FacePointOfInterestPose extends FaceVecPose {

		@Override
		protected Vec3d getFacedVec(PonderScene scene) {
			return scene.getPointOfInterest();
		}

	}

	public static class FaceCursorPose extends FaceVecPose {

		@Override
		protected Vec3d getFacedVec(PonderScene scene) {
			Minecraft minecraft = Minecraft.getInstance();
			MainWindow w = minecraft.getWindow();
			double mouseX = minecraft.mouseHelper.getMouseX() * w.getScaledWidth() / w.getWidth();
			double mouseY = minecraft.mouseHelper.getMouseY() * w.getScaledHeight() / w.getHeight();
			return scene.getTransform()
				.screenToScene(mouseX, mouseY, 300, 0);
		}

	}

}
