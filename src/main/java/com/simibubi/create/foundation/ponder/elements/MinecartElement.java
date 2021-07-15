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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MinecartElement extends AnimatedSceneElement {

	private Vector3d location;
	private LerpedFloat rotation;
	private AbstractMinecartEntity entity;
	private MinecartConstructor constructor;
	private float initialRotation;

	public interface MinecartConstructor {
		AbstractMinecartEntity create(World w, double x, double y, double z);
	}

	public MinecartElement(Vector3d location, float rotation, MinecartConstructor constructor) {
		initialRotation = rotation;
		this.location = location.add(0, 1 / 16f, 0);
		this.constructor = constructor;
		this.rotation = LerpedFloat.angular()
			.startWithValue(rotation);
	}

	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		entity.setPosRaw(0, 0, 0);
		entity.xo = 0;
		entity.yo = 0;
		entity.zo = 0;
		entity.xOld = 0;
		entity.yOld = 0;
		entity.zOld = 0;
		rotation.startWithValue(initialRotation);
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		if (entity == null)
			entity = constructor.create(scene.getWorld(), 0, 0, 0);

		entity.tickCount++;
		entity.onGround = true;
		entity.xo = entity.getX();
		entity.yo = entity.getY();
		entity.zo = entity.getZ();
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

	public void setRotation(float angle, boolean immediate) {
		if (entity == null)
			return;
		rotation.setValue(angle);
		if (!immediate)
			return;
		rotation.startWithValue(angle);
	}

	public Vector3d getPositionOffset() {
		return entity != null ? entity.position() : Vector3d.ZERO;
	}

	public Vector3d getRotation() {
		return new Vector3d(0, rotation.getValue(), 0);
	}

	@Override
	protected void renderLast(PonderWorld world, IRenderTypeBuffer buffer, MatrixStack ms, float fade, float pt) {
		EntityRendererManager entityrenderermanager = Minecraft.getInstance()
			.getEntityRenderDispatcher();
		if (entity == null)
			entity = constructor.create(world, 0, 0, 0);

		ms.pushPose();
		ms.translate(location.x, location.y, location.z);
		ms.translate(MathHelper.lerp(pt, entity.xo, entity.getX()),
			MathHelper.lerp(pt, entity.yo, entity.getY()), MathHelper.lerp(pt, entity.zo, entity.getZ()));

		MatrixStacker.of(ms)
			.rotateY(rotation.getValue(pt));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.popPose();
	}

}
