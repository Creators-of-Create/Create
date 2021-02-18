package com.simibubi.create.content.contraptions.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RotationIndicatorParticle extends SimpleAnimatedParticle {

	protected float radius;
	protected float radius1;
	protected float radius2;
	protected float speed;
	protected Axis axis;
	protected Vec3d origin;
	protected Vec3d offset;
	protected boolean isVisible;

	private RotationIndicatorParticle(World world, double x, double y, double z, int color, float radius1,
			float radius2, float speed, Axis axis, int lifeSpan, boolean isVisible, IAnimatedSprite sprite) {
		super(world, x, y, z, sprite, 0);
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.origin = new Vec3d(x, y, z);
		this.particleScale *= 0.75F;
		this.maxAge = lifeSpan + this.rand.nextInt(32);
		this.setColorFade(color);
		this.setColor(ColorHelper.mixColors(color, 0xFFFFFF, .5f));
		this.selectSpriteWithAge(sprite);
		this.radius1 = radius1;
		this.radius = radius1;
		this.radius2 = radius2;
		this.speed = speed;
		this.axis = axis;
		this.isVisible = isVisible;
		this.offset = axis.isHorizontal() ? new Vec3d(0, 1, 0) : new Vec3d(1, 0, 0);
		move(0, 0, 0);
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
	}

	@Override
	public void tick() {
		super.tick();
		radius += (radius2 - radius) * .1f;
	}
	
	@Override
	public void buildGeometry(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		if (!isVisible)
			return;
		super.buildGeometry(buffer, renderInfo, partialTicks);
	}

	public void move(double x, double y, double z) {
		float time = AnimationTickHolder.getTicks();
		float angle = (float) ((time * speed) % 360) - (speed / 2 * age * (((float) age) / maxAge));
		Vec3d position = VecHelper.rotate(this.offset.scale(radius), angle, axis).add(origin);
		posX = position.x;
		posY = position.y;
		posZ = position.z;
	}

	public static class Factory implements IParticleFactory<RotationIndicatorParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle makeParticle(RotationIndicatorParticleData data, World worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			boolean visible = player != null && GogglesItem.canSeeParticles(player);
			return new RotationIndicatorParticle(worldIn, x, y, z, data.color, data.radius1, data.radius2, data.speed,
					data.getAxis(), data.lifeSpan, visible, this.spriteSet);
		}
	}

}
