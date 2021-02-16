package com.simibubi.create.foundation.ponder.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.command.arguments.EntityAnchorArgument;
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
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getRenderManager();
		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();

		if (entity == null)
			pose.create(world);

		ms.push();
		ms.translate(location.x, location.y, location.z);

		MatrixStacker.of(ms)
			.rotateY(MathHelper.lerp(pt, entity.prevRotationYaw, entity.rotationYaw));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.pop();
	}

	abstract class ParrotPose {

		abstract void tick(PonderScene scene);

		void create(PonderWorld world) {
			entity = new ParrotEntity(EntityType.PARROT, world);
			entity.setVariant(Create.random.nextInt(5));
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
			entity.lookAt(EntityAnchorArgument.Type.EYES, scene.getPointOfInterest());
		}

	}

}
