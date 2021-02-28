package com.simibubi.create.foundation.ponder.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

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

	public static ParrotElement lookAtPOI(Vec3d location) {
		ParrotElement parrotElement = new ParrotElement(location);
		parrotElement.pose = parrotElement.new FacePointOfInterestPose();
		return parrotElement;
	}

	public static ParrotElement spinOnComponent(Vec3d location, BlockPos componentPos) {
		ParrotElement parrotElement = new ParrotElement(location);
		parrotElement.pose = parrotElement.new SpinOnComponentPose(componentPos);
		return parrotElement;
	}

	public static ParrotElement dance(Vec3d location) {
		ParrotElement parrotElement = new ParrotElement(location);
		parrotElement.pose = parrotElement.new DancePose();
		return parrotElement;
	}

	protected ParrotElement(Vec3d location) {
		this.location = location;
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (entity == null)
			return;

		entity.ticksExisted++;
		entity.prevRotationYawHead = entity.rotationYawHead;
		entity.oFlapSpeed = entity.flapSpeed;
		entity.oFlap = entity.flap;
		entity.onGround = true;

		pose.tick(scene);
	}

	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getRenderManager();

		if (entity == null)
			pose.create(world);

		ms.push();
		ms.translate(location.x, location.y, location.z);

		MatrixStacker.of(ms)
			.rotateY(AngleHelper.angleLerp(pt, entity.prevRotationYaw, entity.rotationYaw));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.pop();
	}

	abstract class ParrotPose {

		abstract void tick(PonderScene scene);

		void create(PonderWorld world) {
			entity = new ParrotEntity(EntityType.PARROT, world);
			int nextInt = Create.random.nextInt(5);
			entity.setVariant(nextInt == 1 ? 0 : nextInt); // blue parrots are kinda hard to see
		}

	}

	class DancePose extends ParrotPose {

		@Override
		void create(PonderWorld world) {
			super.create(world);
			entity.setPartying(BlockPos.ZERO, true);
		}

		@Override
		void tick(PonderScene scene) {
			entity.prevRotationYaw = entity.rotationYaw;
			entity.rotationYaw -= 2;
		}

	}

	class SpinOnComponentPose extends ParrotPose {

		private BlockPos componentPos;

		public SpinOnComponentPose(BlockPos componentPos) {
			this.componentPos = componentPos;
		}

		@Override
		void tick(PonderScene scene) {
			TileEntity tileEntity = scene.getWorld()
				.getTileEntity(componentPos);
			if (!(tileEntity instanceof KineticTileEntity))
				return;
			float rpm = ((KineticTileEntity) tileEntity).getSpeed();
			entity.prevRotationYaw = entity.rotationYaw;
			entity.rotationYaw += (rpm * .3f);
		}

	}

	class FacePointOfInterestPose extends ParrotPose {

		@Override
		void tick(PonderScene scene) {
			Vec3d p_200602_2_ = scene.getPointOfInterest();
			Vec3d vec3d = location.add(entity.getEyePosition(0));
			double d0 = p_200602_2_.x - vec3d.x;
			double d1 = p_200602_2_.y - vec3d.y;
			double d2 = p_200602_2_.z - vec3d.z;
			double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
			entity.prevRotationYaw = entity.rotationYaw;
			entity.prevRotationPitch = entity.rotationPitch;
			entity.rotationPitch =
				MathHelper.wrapDegrees((float) -(MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
			entity.rotationYaw =
				MathHelper.wrapDegrees((float) -(MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) + 90);
		}

	}

}
