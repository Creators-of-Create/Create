package com.simibubi.create.content.trains;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.createmod.catnip.enums.CatnipSpecialTextures;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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

	private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder builder, TextureManager textureManager) {
			CatnipSpecialTextures.BLANK.bind();

			// transparent, additive blending
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);

			// opaque
//			RenderSystem.depthMask(true);
//			RenderSystem.disableBlend();
//			RenderSystem.enableLighting();

			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		}
	};

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
					Vec3 diff = Vec3.atLowerCornerOf(new BlockPos(x, y, z)).add(0.5, 0.5, 0.5).subtract(x, y, z);
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
	public void render(VertexConsumer builder, Camera renderInfo, float p_225606_3_) {
		Vec3 projectedView = renderInfo.getPosition();
		float lerpedX = (float) (Mth.lerp(p_225606_3_, this.xo, this.x) - projectedView.x());
		float lerpedY = (float) (Mth.lerp(p_225606_3_, this.yo, this.y) - projectedView.y());
		float lerpedZ = (float) (Mth.lerp(p_225606_3_, this.zo, this.z) - projectedView.z());

		// int light = getBrightnessForRender(p_225606_3_);
		int light = LightTexture.FULL_BRIGHT;
		double ageMultiplier = 1 - Math.pow(Mth.clamp(age + p_225606_3_, 0, lifetime), 3) / Math.pow(lifetime, 3);

		for (int i = 0; i < 6; i++) {
			// 6 faces to a cube
			for (int j = 0; j < 4; j++) {
				Vec3 vec = CUBE[i * 4 + j].scale(-1);
				vec = vec
					/* .rotate(?) */
					.scale(scale * ageMultiplier)
					.add(lerpedX, lerpedY, lerpedZ);

				builder.vertex(vec.x, vec.y, vec.z)
					.uv(j / 2, j % 2)
					.color(rCol, gCol, bCol, alpha)
					.uv2(light)
					.endVertex();
			}
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return RENDER_TYPE;
	}

	public static class Factory implements ParticleProvider<CubeParticleData> {

		@Override
		public Particle createParticle(CubeParticleData data, ClientLevel world, double x, double y, double z, double motionX,
			double motionY, double motionZ) {
			CubeParticle particle = new CubeParticle(world, x, y, z, motionX, motionY, motionZ);
			particle.setColor(data.r, data.g, data.b);
			particle.setScale(data.scale);
			particle.averageAge(data.avgAge);
			particle.setHot(data.hot);
			return particle;
		}
	}
}
