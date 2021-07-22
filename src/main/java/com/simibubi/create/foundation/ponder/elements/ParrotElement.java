package com.simibubi.create.foundation.ponder.elements;

import java.util.function.Supplier;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ParrotElement extends AnimatedSceneElement {

	private Vector3d location;
	private ParrotEntity entity;
	private ParrotPose pose;
	private Supplier<? extends ParrotPose> initialPose;

	public static ParrotElement create(Vector3d location, Supplier<? extends ParrotPose> pose) {
		return new ParrotElement(location, pose);
	}

	protected ParrotElement(Vector3d location, Supplier<? extends ParrotPose> pose) {
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
		entity.xRotO = entity.xRot = 0;
		entity.yRotO = entity.yRot = 180;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (entity == null) {
			entity = pose.create(scene.getWorld());
			entity.yRotO = entity.yRot = 180;
		}

		entity.tickCount++;
		entity.yHeadRotO = entity.yHeadRot;
		entity.oFlapSpeed = entity.flapSpeed;
		entity.oFlap = entity.flap;
		entity.onGround = true;

		entity.xo = entity.getX();
		entity.yo = entity.getY();
		entity.zo = entity.getZ();
		entity.yRotO = entity.yRot;
		entity.xRotO = entity.xRot;

		pose.tick(scene, entity, location);

		entity.xOld = entity.getX();
		entity.yOld = entity.getY();
		entity.zOld = entity.getZ();
	}

	public void setPositionOffset(Vector3d position, boolean immediate) {
		if (entity == null)
			return;
		entity.setPos(position.x, position.y, position.z);
		if (!immediate)
			return;
		entity.xo = position.x;
		entity.yo = position.y;
		entity.zo = position.z;
	}

	public void setRotation(Vector3d eulers, boolean immediate) {
		if (entity == null)
			return;
		entity.xRot = (float) eulers.x;
		entity.yRot = (float) eulers.y;
		if (!immediate)
			return;
		entity.xRotO = entity.xRot;
		entity.yRotO = entity.yRot;
	}

	public Vector3d getPositionOffset() {
		return entity != null ? entity.position() : Vector3d.ZERO;
	}

	public Vector3d getRotation() {
		return entity != null ? new Vector3d(entity.xRot, entity.yRot, 0) : Vector3d.ZERO;
	}

	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getEntityRenderDispatcher();

		if (entity == null) {
			entity = pose.create(world);
			entity.yRotO = entity.yRot = 180;
		}

		ms.pushPose();
		ms.translate(location.x, location.y, location.z);
		ms.translate(MathHelper.lerp(pt, entity.xo, entity.getX()),
			MathHelper.lerp(pt, entity.yo, entity.getY()), MathHelper.lerp(pt, entity.zo, entity.getZ()));

		MatrixTransformStack.of(ms)
			.rotateY(AngleHelper.angleLerp(pt, entity.yRotO, entity.yRot));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.popPose();
	}

	public void setPose(ParrotPose pose) {
		this.pose = pose;
	}

	public static abstract class ParrotPose {

		abstract void tick(PonderScene scene, ParrotEntity entity, Vector3d location);

		ParrotEntity create(PonderWorld world) {
			ParrotEntity entity = new ParrotEntity(EntityType.PARROT, world);
			int nextInt = Create.RANDOM.nextInt(5);
			entity.setVariant(nextInt == 1 ? 0 : nextInt); // blue parrots are kinda hard to see
			return entity;
		}

	}

	public static class DancePose extends ParrotPose {

		@Override
		ParrotEntity create(PonderWorld world) {
			ParrotEntity entity = super.create(world);
			entity.setRecordPlayingNearby(BlockPos.ZERO, true);
			return entity;
		}

		@Override
		void tick(PonderScene scene, ParrotEntity entity, Vector3d location) {
			entity.yRotO = entity.yRot;
			entity.yRot -= 2;
		}

	}

	public static class FlappyPose extends ParrotPose {

		@Override
		void tick(PonderScene scene, ParrotEntity entity, Vector3d location) {
			double length = entity.position()
				.subtract(entity.xOld, entity.yOld, entity.zOld)
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
		void tick(PonderScene scene, ParrotEntity entity, Vector3d location) {
			TileEntity tileEntity = scene.getWorld()
				.getBlockEntity(componentPos);
			if (!(tileEntity instanceof KineticTileEntity))
				return;
			float rpm = ((KineticTileEntity) tileEntity).getSpeed();
			entity.yRotO = entity.yRot;
			entity.yRot += (rpm * .3f);
		}

	}

	public static abstract class FaceVecPose extends ParrotPose {

		@Override
		void tick(PonderScene scene, ParrotEntity entity, Vector3d location) {
			Vector3d p_200602_2_ = getFacedVec(scene);
			Vector3d Vector3d = location.add(entity.getEyePosition(0));
			double d0 = p_200602_2_.x - Vector3d.x;
			double d1 = p_200602_2_.y - Vector3d.y;
			double d2 = p_200602_2_.z - Vector3d.z;
			double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
			float targetPitch =
				MathHelper.wrapDegrees((float) -(MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
			float targetYaw =
				MathHelper.wrapDegrees((float) -(MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) + 90);

			entity.xRot = AngleHelper.angleLerp(.4f, entity.xRot, targetPitch);
			entity.yRot = AngleHelper.angleLerp(.4f, entity.yRot, targetYaw);
		}

		protected abstract Vector3d getFacedVec(PonderScene scene);

	}

	public static class FacePointOfInterestPose extends FaceVecPose {

		@Override
		protected Vector3d getFacedVec(PonderScene scene) {
			return scene.getPointOfInterest();
		}

	}

	public static class FaceCursorPose extends FaceVecPose {

		@Override
		protected Vector3d getFacedVec(PonderScene scene) {
			Minecraft minecraft = Minecraft.getInstance();
			MainWindow w = minecraft.getWindow();
			double mouseX = minecraft.mouseHandler.xpos() * w.getGuiScaledWidth() / w.getScreenWidth();
			double mouseY = minecraft.mouseHandler.ypos() * w.getGuiScaledHeight() / w.getScreenHeight();
			return scene.getTransform()
				.screenToScene(mouseX, mouseY, 300, 0);
		}

	}

}
