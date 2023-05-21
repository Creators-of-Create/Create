package com.simibubi.create.content.equipment.potatoCannon;

import static com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileRenderMode.entityRandom;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface PotatoProjectileRenderMode {

	@OnlyIn(Dist.CLIENT)
	void transform(PoseStack ms, PotatoProjectileEntity entity, float pt);

	public static class Billboard implements PotatoProjectileRenderMode {

		public static final Billboard INSTANCE = new Billboard();

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			Minecraft mc = Minecraft.getInstance();
			Vec3 p1 = mc.getCameraEntity()
				.getEyePosition(pt);
			Vec3 diff = entity.getBoundingBox()
				.getCenter()
				.subtract(p1);

			TransformStack.cast(ms)
				.rotateY(AngleHelper.deg(Mth.atan2(diff.x, diff.z)) + 180)
				.rotateX(AngleHelper.deg(Mth.atan2(diff.y, Mth.sqrt((float) (diff.x * diff.x + diff.z * diff.z)))));
		}

	}

	public static class Tumble extends Billboard {

		public static final Tumble INSTANCE = new Tumble();

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			super.transform(ms, entity, pt);
			TransformStack.cast(ms)
				.rotateZ((entity.tickCount + pt) * 2 * entityRandom(entity, 16))
				.rotateX((entity.tickCount + pt) * entityRandom(entity, 32));
		}

	}

	public static class TowardMotion implements PotatoProjectileRenderMode {

		private int spriteAngleOffset;
		private float spin;

		public TowardMotion(int spriteAngleOffset, float spin) {
			this.spriteAngleOffset = spriteAngleOffset;
			this.spin = spin;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			Vec3 diff = entity.getDeltaMovement();
			TransformStack.cast(ms)
				.rotateY(AngleHelper.deg(Mth.atan2(diff.x, diff.z)))
				.rotateX(270
					+ AngleHelper.deg(Mth.atan2(diff.y, -Mth.sqrt((float) (diff.x * diff.x + diff.z * diff.z)))));
			TransformStack.cast(ms)
				.rotateY((entity.tickCount + pt) * 20 * spin + entityRandom(entity, 360))
				.rotateZ(-spriteAngleOffset);
		}

	}

	public static class StuckToEntity implements PotatoProjectileRenderMode {

		private Vec3 offset;

		public StuckToEntity(Vec3 offset) {
			this.offset = offset;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			TransformStack.cast(ms).rotateY(AngleHelper.deg(Mth.atan2(offset.x, offset.z)));
		}

	}

	public static int entityRandom(Entity entity, int maxValue) {
		return (System.identityHashCode(entity) * 31) % maxValue;
	}

}
