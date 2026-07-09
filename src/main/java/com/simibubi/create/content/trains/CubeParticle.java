package com.simibubi.create.content.trains;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class CubeParticle extends Particle {

	public static final Vec3[] CUBE = {
		// TOP
		new Vec3(1, 1, -1), new Vec3(1, 1, 1), new Vec3(-1, 1, 1), new Vec3(-1, 1, -1),

		// BOTTOM
		new Vec3(-1, -1, -1), new Vec3(-1, -1, 1), new Vec3(1, -1, 1), new Vec3(1, -1, -1),

		// FRONT
		new Vec3(-1, -1, 1), new Vec3(-1, 1, 1), new Vec3(1, 1, 1), new Vec3(1, -1, 1),

		// BACK
		new Vec3(1, -1, -1), new Vec3(1, 1, -1), new Vec3(-1, 1, -1), new Vec3(-1, -1, -1),

		// LEFT
		new Vec3(-1, -1, -1), new Vec3(-1, 1, -1), new Vec3(-1, 1, 1), new Vec3(-1, -1, 1),

		// RIGHT
		new Vec3(1, -1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, -1), new Vec3(1, -1, -1) };

	protected float scale;
	protected boolean hot;

	public CubeParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z);
		this.xd = motionX;
		this.yd = motionY;
		this.zd = motionZ;

		setScale(0.2F);
	}

	public void setScale(float scale) {
		this.scale = scale;
		this.setSize(scale * 0.5f, scale * 0.5f);
	}

	public void averageAge(int age) {
		this.lifetime = (int) (age + (random.nextDouble() * 2D - 1D) * 8);
	}

	public void setHot(boolean hot) {
		this.hot = hot;
	}

	private boolean billowing = false;

	@Override
	public void tick() {
		if (this.hot && this.age > 0) {
			if (this.yo == this.y) {
				billowing = true;
				stoppedByCollision = false; // Prevent motion being ignored due to vertical collision
				if (this.xd == 0 && this.zd == 0) {
					Vec3 diff = Vec3.atLowerCornerOf(BlockPos.containing(x, y, z))
						.add(0.5, 0.5, 0.5)
						.subtract(x, y, z);
					this.xd = -diff.x * 0.1;
					this.zd = -diff.z * 0.1;
				}
				this.xd *= 1.1;
				this.yd *= 0.9;
				this.zd *= 1.1;
			} else if (billowing) {
				this.yd *= 1.2;
			}
		}
		super.tick();
	}

	@Override
	public @NotNull ParticleRenderType getGroup() {
		// TODO 26.2: Rebuild cube particle extraction for the new particle render-state pipeline.
		return ParticleRenderType.NO_RENDER;
	}

	public static class Factory implements ParticleProvider<CubeParticleData> {

		@Override
		public Particle createParticle(CubeParticleData data, ClientLevel world, double x, double y, double z, double motionX,
			double motionY, double motionZ, RandomSource random) {
			CubeParticle particle = new CubeParticle(world, x, y, z, motionX, motionY, motionZ);
			particle.setScale(data.scale);
			particle.averageAge(data.avgAge);
			particle.setHot(data.hot);
			return particle;
		}
	}
}
