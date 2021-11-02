package com.simibubi.create.content.contraptions.particle;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class AirParticle extends SimpleAnimatedParticle {

	private float originX, originY, originZ;
	private float targetX, targetY, targetZ;
	private float drag;

	private float twirlRadius, twirlAngleOffset;
	private Axis twirlAxis;

	protected AirParticle(ClientWorld world, AirParticleData data, double x, double y, double z, double dx, double dy,
						  double dz, IAnimatedSprite sprite) {
		super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
		quadSize *= 0.75F;
		hasPhysics = false;

		setPos(x, y, z);
		originX = (float) (xo = x);
		originY = (float) (yo = y);
		originZ = (float) (zo = z);
		targetX = (float) (x + dx);
		targetY = (float) (y + dy);
		targetZ = (float) (z + dz);
		drag = data.drag;

		twirlRadius = Create.RANDOM.nextFloat() / 6;
		twirlAngleOffset = Create.RANDOM.nextFloat() * 360;
		twirlAxis = Create.RANDOM.nextBoolean() ? Axis.X : Axis.Z;

		// speed in m/ticks
		double length = new Vector3d(dx, dy, dz).length();
		lifetime = Math.min((int) (length / data.speed), 60);
		selectSprite(7);
		setAlpha(.25f);
		
		if (length == 0) {
			remove();
			setAlpha(0);
		}
	}

	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}

		float progress = (float) Math.pow(((float) age) / lifetime, drag);
		float angle = (progress * 2 * 360 + twirlAngleOffset) % 360;
		Vector3d twirl = VecHelper.rotate(new Vector3d(0, twirlRadius, 0), angle, twirlAxis);
		
		float x = (float) (MathHelper.lerp(progress, originX, targetX) + twirl.x);
		float y = (float) (MathHelper.lerp(progress, originY, targetY) + twirl.y);
		float z = (float) (MathHelper.lerp(progress, originZ, targetZ) + twirl.z);
		
		xd = x - this.x;
		yd = y - this.y;
		zd = z - this.z;

		setSpriteFromAge(sprites);
		this.move(this.xd, this.yd, this.zd);
	}

	public int getLightColor(float partialTick) {
		BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
		return this.level.isLoaded(blockpos) ? WorldRenderer.getLightColor(level, blockpos) : 0;
	}

	private void selectSprite(int index) {
		setSprite(sprites.get(index, 8));
	}

	public static class Factory implements IParticleFactory<AirParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(AirParticleData data, ClientWorld worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
			return new AirParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

}
