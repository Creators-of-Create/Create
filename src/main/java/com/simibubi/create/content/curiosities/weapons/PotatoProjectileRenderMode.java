package com.simibubi.create.content.curiosities.weapons;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
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
			Vector3d p1 = mc.getRenderViewEntity()
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
				.rotateZ((entity.ticksExisted + pt) * 2 * (entity.getEntityId() % 16))
				.rotateX((entity.ticksExisted + pt) * (entity.getEntityId() % 32));
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
			Vector3d diff = entity.getMotion();
			MatrixStacker.of(ms)
				.rotateY(AngleHelper.deg(MathHelper.atan2(diff.x, diff.z)))
				.rotateX(270
					+ AngleHelper.deg(MathHelper.atan2(diff.y, -MathHelper.sqrt(diff.x * diff.x + diff.z * diff.z))));
			MatrixStacker.of(ms)
				.rotateY((entity.ticksExisted + pt) * 20 * spin)
				.rotateZ(-spriteAngleOffset);
		}

	}

}
