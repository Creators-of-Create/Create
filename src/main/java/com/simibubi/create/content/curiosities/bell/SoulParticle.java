package com.simibubi.create.content.curiosities.bell;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class SoulParticle extends CustomRotationParticle {

	private final IAnimatedSprite animatedSprite;

	protected int startTicks;
	protected int endTicks;
	protected int numLoops;

	protected int firstStartFrame = 0;
	protected int startFrames = 17;

	protected int firstLoopFrame = 17;
	protected int loopFrames = 16;

	protected int firstEndFrame = 33;
	protected int endFrames = 20;

	protected AnimationStage animationStage;

	protected int totalFrames = 53;
	protected int ticksPerFrame = 2;

	protected boolean isPerimeter = false;
	protected boolean isExpandingPerimeter = false;
	protected boolean isVisible = true;
	protected int perimeterFrames = 8;

	public SoulParticle(ClientWorld worldIn, double x, double y, double z, double vx, double vy, double vz,
		IAnimatedSprite spriteSet, IParticleData data) {
		super(worldIn, x, y, z, spriteSet, 0);
		this.animatedSprite = spriteSet;
		this.particleScale = 0.5f;
		this.setSize(this.particleScale, this.particleScale);

		this.loopLength = loopFrames + (int) (this.rand.nextFloat() * 5f - 4f);
		this.startTicks = startFrames + (int) (this.rand.nextFloat() * 5f - 4f);
		this.endTicks = endFrames + (int) (this.rand.nextFloat() * 5f - 4f);
		this.numLoops = (int) (1f + this.rand.nextFloat() * 2f);

		this.setFrame(0);
		this.field_21507 = true; // disable movement
		this.mirror = this.rand.nextBoolean();

		this.isPerimeter = data instanceof PerimeterData;
		this.isExpandingPerimeter = data instanceof ExpandingPerimeterData;
		this.animationStage = !isPerimeter ? new StartAnimation(this) : new PerimeterAnimation(this);
		if (isPerimeter) {
			prevPosY = posY -= .5f - 1 / 128f;
			totalFrames = perimeterFrames;
			isVisible = false;
		}
	}

	@Override
	public void tick() {
		animationStage.tick();
		animationStage = animationStage.getNext();

		BlockPos pos = new BlockPos(posX, posY, posZ);
		if (animationStage == null)
			setExpired();
		if (!SoulPulseEffect.isDark(world, pos)) {
			isVisible = true;
			if (!isPerimeter)
				setExpired();
		} else if (isPerimeter)
			isVisible = false;
	}

	@Override
	public void buildGeometry(IVertexBuilder builder, ActiveRenderInfo camera, float partialTicks) {
		if (!isVisible)
			return;
		super.buildGeometry(builder, camera, partialTicks);
	}

	public void setFrame(int frame) {
		if (frame >= 0 && frame < totalFrames)
			setSprite(animatedSprite.get(frame, totalFrames));
	}

	@Override
	public Quaternion getCustomRotation(ActiveRenderInfo camera, float partialTicks) {
		if (isPerimeter)
			return Vector3f.POSITIVE_X.getDegreesQuaternion(90);
		return new Quaternion(0, -camera.getYaw(), 0, true);
	}

	public static class Data extends BasicParticleData<SoulParticle> {
		@Override
		public IBasicParticleFactory<SoulParticle> getBasicFactory() {
			return (worldIn, x, y, z, vx, vy, vz, spriteSet) -> new SoulParticle(worldIn, x, y, z, vx, vy, vz,
				spriteSet, this);
		}

		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL.get();
		}
	}

	public static class PerimeterData extends BasicParticleData<SoulParticle> {
		@Override
		public IBasicParticleFactory<SoulParticle> getBasicFactory() {
			return (worldIn, x, y, z, vx, vy, vz, spriteSet) -> new SoulParticle(worldIn, x, y, z, vx, vy, vz,
				spriteSet, this);
		}

		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL_PERIMETER.get();
		}
	}

	public static class ExpandingPerimeterData extends PerimeterData {
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL_EXPANDING_PERIMETER.get();
		}
	}

	public static abstract class AnimationStage {

		protected final SoulParticle particle;

		protected int ticks;
		protected int animAge;

		public AnimationStage(SoulParticle particle) {
			this.particle = particle;
		}

		public void tick() {
			ticks++;

			if (ticks % particle.ticksPerFrame == 0)
				animAge++;
		}

		public float getAnimAge() {
			return (float) animAge;
		}

		public abstract AnimationStage getNext();
	}

	public static class StartAnimation extends AnimationStage {

		public StartAnimation(SoulParticle particle) {
			super(particle);
		}

		@Override
		public void tick() {
			super.tick();

			particle.setFrame(
				particle.firstStartFrame + (int) (getAnimAge() / (float) particle.startTicks * particle.startFrames));
		}

		@Override
		public AnimationStage getNext() {
			if (animAge < particle.startTicks)
				return this;
			else
				return new LoopAnimation(particle);
		}
	}

	public static class LoopAnimation extends AnimationStage {

		int loops;

		public LoopAnimation(SoulParticle particle) {
			super(particle);
		}

		@Override
		public void tick() {
			super.tick();

			int loopTick = getLoopTick();

			if (loopTick == 0)
				loops++;

			particle.setFrame(particle.firstLoopFrame + loopTick);// (int) (((float) loopTick / (float)
																	// particle.loopLength) * particle.loopFrames));

		}

		private int getLoopTick() {
			return animAge % particle.loopFrames;
		}

		@Override
		public AnimationStage getNext() {
			if (loops <= particle.numLoops)
				return this;
			else
				return new EndAnimation(particle);
		}
	}

	public static class EndAnimation extends AnimationStage {

		public EndAnimation(SoulParticle particle) {
			super(particle);
		}

		@Override
		public void tick() {
			super.tick();

			particle.setFrame(
				particle.firstEndFrame + (int) ((getAnimAge() / (float) particle.endTicks) * particle.endFrames));

		}

		@Override
		public AnimationStage getNext() {
			if (animAge < particle.endTicks)
				return this;
			else
				return null;
		}
	}

	public static class PerimeterAnimation extends AnimationStage {

		public PerimeterAnimation(SoulParticle particle) {
			super(particle);
		}

		@Override
		public void tick() {
			super.tick();
			particle.setFrame((int) getAnimAge() % particle.perimeterFrames);
		}

		@Override
		public AnimationStage getNext() {
			if (animAge < (particle.isExpandingPerimeter ? 8
				: particle.startTicks + particle.endTicks + particle.numLoops * particle.loopLength))
				return this;
			else
				return null;
		}
	}
}
