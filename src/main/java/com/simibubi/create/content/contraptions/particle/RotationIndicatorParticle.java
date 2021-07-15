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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.vector.Vector3d;

public class RotationIndicatorParticle extends SimpleAnimatedParticle {

	protected float radius;
	protected float radius1;
	protected float radius2;
	protected float speed;
	protected Axis axis;
	protected Vector3d origin;
	protected Vector3d offset;
	protected boolean isVisible;

	private RotationIndicatorParticle(ClientWorld world, double x, double y, double z, int color, float radius1,
									  float radius2, float speed, Axis axis, int lifeSpan, boolean isVisible, IAnimatedSprite sprite) {
		super(world, x, y, z, sprite, 0);
		this.xd = 0;
		this.yd = 0;
		this.zd = 0;
		this.origin = new Vector3d(x, y, z);
		this.quadSize *= 0.75F;
		this.lifetime = lifeSpan + this.random.nextInt(32);
		this.setFadeColor(color);
		this.setColor(ColorHelper.mixColors(color, 0xFFFFFF, .5f));
		this.setSpriteFromAge(sprite);
		this.radius1 = radius1;
		this.radius = radius1;
		this.radius2 = radius2;
		this.speed = speed;
		this.axis = axis;
		this.isVisible = isVisible;
		this.offset = axis.isHorizontal() ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);
		move(0, 0, 0);
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
	}

	@Override
	public void tick() {
		super.tick();
		radius += (radius2 - radius) * .1f;
	}

	@Override
	public void render(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		if (!isVisible)
			return;
		super.render(buffer, renderInfo, partialTicks);
	}

	public void move(double x, double y, double z) {
		float time = AnimationTickHolder.getTicks(level);
		float angle = (float) ((time * speed) % 360) - (speed / 2 * age * (((float) age) / lifetime));
		if (speed < 0 && axis.isVertical())
			angle += 180;
		Vector3d position = VecHelper.rotate(this.offset.scale(radius), angle, axis).add(origin);
		x = position.x;
		y = position.y;
		z = position.z;
	}

	public static class Factory implements IParticleFactory<RotationIndicatorParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(RotationIndicatorParticleData data, ClientWorld worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			Minecraft mc = Minecraft.getInstance();
			ClientPlayerEntity player = mc.player;
			boolean visible = worldIn != mc.level || player != null && GogglesItem.canSeeParticles(player);
			return new RotationIndicatorParticle(worldIn, x, y, z, data.color, data.radius1, data.radius2, data.speed,
				data.getAxis(), data.lifeSpan, visible, this.spriteSet);
		}
	}

}
