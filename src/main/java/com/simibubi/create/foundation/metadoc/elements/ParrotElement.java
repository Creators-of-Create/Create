package com.simibubi.create.foundation.metadoc.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.metadoc.MetaDocWorld;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ParrotElement extends AnimatedSceneElement {

	private Vec3d location;
	private ParrotEntity entity;

	public ParrotElement(Vec3d location) {
		this.location = location;
	}

	@Override
	public void tick() {
		super.tick();
		if (entity == null)
			return;
		entity.ticksExisted++;

//		entity.prevRotationYawHead = entity.rotationYawHead;
		entity.oFlapSpeed = entity.flapSpeed;
		entity.oFlap = entity.flap;
		entity.onGround = true;

//		entity.rotationYawHead++;
		entity.flapSpeed = .5f;
		entity.flap = 1;
//		entity.flap += entity.flapSpeed;
//		entity.flap += .5f + Create.random.nextFloat();
	}

	@Override
	protected void render(MetaDocWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getRenderManager();
		float pt = Minecraft.getInstance()
			.getRenderPartialTicks();
		
		if (entity == null) {
			entity = new ParrotEntity(EntityType.PARROT, world);
			entity.setVariant(Create.random.nextInt(5));
//			entity.setPartying(BlockPos.ZERO, true);
		}

		ms.push();
		ms.translate(location.x, location.y, location.z);

		MatrixStacker.of(ms)
			.rotateY(AnimationTickHolder.getRenderTick() * 15)
			.rotateZ(30);
		ms.translate(-.25f, 0, 0);
		
		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.pop();
	}

}
