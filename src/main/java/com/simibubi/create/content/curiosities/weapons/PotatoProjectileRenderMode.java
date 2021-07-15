package com.simibubi.create.content.curiosities.weapons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class PotatoProjectileRenderMode {

	@OnlyIn(Dist.CLIENT)
	public abstract void transform(MatrixStack ms, PotatoProjectileEntity entity, float pt);

	public static class Billboard extends PotatoProjectileRenderMode {

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(MatrixStack ms, PotatoProjectileEntity entity, float pt) {
			Minecraft mc = Minecraft.getInstance();
			Vector3d p1 = mc.getCameraEntity()
				.getEyePosition(pt);
			Vector3d diff = entity.getBoundingBox()
				.getCenter()
				.subtract(p1);

			MatrixStacker.of(ms)
				.rotateY(AngleHelper.deg(MathHelper.atan2(diff.x, diff.z)))
				.rotateX(180
					+ AngleHelper.deg(MathHelper.atan2(diff.y, -MathHelper.sqrt(diff.x * diff.x + diff.z * diff.z))));
		}
	}

	public static class Tumble extends Billboard {

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(MatrixStack ms, PotatoProjectileEntity entity, float pt) {
			super.transform(ms, entity, pt);
			MatrixStacker.of(ms)
				.rotateZ((entity.tickCount + pt) * 2 * entityRandom(entity, 16))
				.rotateX((entity.tickCount + pt) * entityRandom(entity, 32));
		}
	}

	public static class TowardMotion extends PotatoProjectileRenderMode {

		private int spriteAngleOffset;
		private float spin;

		public TowardMotion(int spriteAngleOffset, float spin) {
			this.spriteAngleOffset = spriteAngleOffset;
			this.spin = spin;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(MatrixStack ms, PotatoProjectileEntity entity, float pt) {
			Vector3d diff = entity.getDeltaMovement();
			MatrixStacker.of(ms)
				.rotateY(AngleHelper.deg(MathHelper.atan2(diff.x, diff.z)))
				.rotateX(270
					+ AngleHelper.deg(MathHelper.atan2(diff.y, -MathHelper.sqrt(diff.x * diff.x + diff.z * diff.z))));
			MatrixStacker.of(ms)
				.rotateY((entity.tickCount + pt) * 20 * spin + entityRandom(entity, 360))
				.rotateZ(-spriteAngleOffset);
		}

	}

	public static class StuckToEntity extends PotatoProjectileRenderMode {

		private Vector3d offset;

		public StuckToEntity(Vector3d offset) {
			this.offset = offset;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(MatrixStack ms, PotatoProjectileEntity entity, float pt) {
			MatrixStacker.of(ms).rotateY(AngleHelper.deg(MathHelper.atan2(offset.x, offset.z)));
		}

	}

	public static int entityRandom(Entity entity, int maxValue) {
		return (System.identityHashCode(entity) * 31) % maxValue;
	}

}
