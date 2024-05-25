package com.simibubi.create.foundation.ponder.element;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MinecartElement extends AnimatedSceneElement {

	private Vec3 location;
	private LerpedFloat rotation;
	private AbstractMinecart entity;
	private MinecartConstructor constructor;
	private float initialRotation;

	public interface MinecartConstructor {
		AbstractMinecart create(Level w, double x, double y, double z);
	}

	public MinecartElement(Vec3 location, float rotation, MinecartConstructor constructor) {
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
		entity.setOnGround(true);
		entity.xo = entity.getX();
		entity.yo = entity.getY();
		entity.zo = entity.getZ();
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

	public void setRotation(float angle, boolean immediate) {
		if (entity == null)
			return;
		rotation.setValue(angle);
		if (!immediate)
			return;
		rotation.startWithValue(angle);
	}

	public Vec3 getPositionOffset() {
		return entity != null ? entity.position() : Vec3.ZERO;
	}

	public Vec3 getRotation() {
		return new Vec3(0, rotation.getValue(), 0);
	}

	@Override
	protected void renderLast(PonderWorld world, MultiBufferSource buffer, PoseStack ms, float fade, float pt) {
		EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance()
			.getEntityRenderDispatcher();
		if (entity == null)
			entity = constructor.create(world, 0, 0, 0);

		ms.pushPose();
		ms.translate(location.x, location.y, location.z);
		ms.translate(Mth.lerp(pt, entity.xo, entity.getX()),
			Mth.lerp(pt, entity.yo, entity.getY()), Mth.lerp(pt, entity.zo, entity.getZ()));

		TransformStack.of(ms)
			.rotateYDegrees(rotation.getValue(pt));

		entityrenderermanager.render(entity, 0, 0, 0, 0, pt, ms, buffer, lightCoordsFromFade(fade));
		ms.popPose();
	}

}
