package com.simibubi.create.foundation.ponder.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MinecartElement extends AnimatedSceneElement {

	private Vec3d location;
	private LerpedFloat rotation;
	private AbstractMinecartEntity entity;
	private MinecartConstructor constructor;
	private float initialRotation;

	public interface MinecartConstructor {
		AbstractMinecartEntity create(World w, double x, double y, double z);
	}

	public MinecartElement(Vec3d location, float rotation, MinecartConstructor constructor) {
		initialRotation = rotation;
		this.location = location.add(0, 1 / 16f, 0);
		this.constructor = constructor;
		this.rotation = LerpedFloat.angular()
			.startWithValue(rotation);
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		entity.setPos(0, 0, 0);
		entity.prevPosX = 0;
		entity.prevPosY = 0;
		entity.prevPosZ = 0;
		entity.lastTickPosX = 0;
		entity.lastTickPosY = 0;
		entity.lastTickPosZ = 0;
		rotation.startWithValue(initialRotation);
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (entity == null)
			entity = constructor.create(scene.getWorld(), 0, 0, 0);

		entity.ticksExisted++;
		entity.onGround = true;
		entity.prevPosX = entity.getX();
		entity.prevPosY = entity.getY();
		entity.prevPosZ = entity.getZ();
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

	public void setRotation(float angle, boolean immediate) {
		if (entity == null)
			return;
		rotation.setValue(angle);
		if (!immediate)
			return;
		rotation.startWithValue(angle);
	}

	public Vec3d getPositionOffset() {
		return entity != null ? entity.getPositionVec() : Vec3d.ZERO;
	}

	public Vec3d getRotation() {
		return new Vec3d(0, rotation.getValue(), 0);
	}

	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getRenderManager();
		if (entity == null)
			entity = constructor.create(world, 0, 0, 0);

		ms.push();
		ms.translate(location.x, location.y, location.z);
		ms.translate(MathHelper.lerp(pt, entity.prevPosX, entity.getX()),
			MathHelper.lerp(pt, entity.prevPosY, entity.getY()), MathHelper.lerp(pt, entity.prevPosZ, entity.getZ()));

		MatrixStacker.of(ms)
			.rotateY(rotation.getValue(pt));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.pop();
	}

}
