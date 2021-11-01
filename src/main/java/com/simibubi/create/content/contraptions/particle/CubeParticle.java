package com.simibubi.create.content.contraptions.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.texture.TextureManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.multiplayer.ClientLevel;
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

	public static final Vec3[] CUBE_NORMALS = {
		// modified normals for the sides
		new Vec3(0, 1, 0), new Vec3(0, -1, 0), new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1),
		new Vec3(0, 0, 1),

		/*
		 * new Vector3d(0, 1, 0), new Vector3d(0, -1, 0), new Vector3d(0, 0, 1), new Vector3d(0, 0,
		 * -1), new Vector3d(-1, 0, 0), new Vector3d(1, 0, 0)
		 */
	};

	private static final ParticleRenderType renderType = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder builder, TextureManager textureManager) {
			RenderSystem.disableTexture();

			// transparent, additive blending
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			RenderSystem.enableLighting();
			RenderSystem.enableColorMaterial();

			// opaque
//			RenderSystem.depthMask(true);
//			RenderSystem.disableBlend();
//			RenderSystem.enableLighting();

			builder.begin(GL11.GL_QUADS, DefaultVertexFormat.BLOCK);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.disableLighting();
			RenderSystem.enableTexture();
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
		int light = 15728880;// 15<<20 && 15<<4
		double ageMultiplier = 1 - Math.pow(age, 3) / Math.pow(lifetime, 3);

		for (int i = 0; i < 6; i++) {
			// 6 faces to a cube
			for (int j = 0; j < 4; j++) {
				Vec3 vec = CUBE[i * 4 + j];
				vec = vec
					/* .rotate(?) */
					.scale(scale * ageMultiplier)
					.add(lerpedX, lerpedY, lerpedZ);

				Vec3 normal = CUBE_NORMALS[i];
				builder.vertex(vec.x, vec.y, vec.z)
					.color(rCol, gCol, bCol, alpha)
					.uv(0, 0)
					.uv2(light)
					.normal((float) normal.x, (float) normal.y, (float) normal.z)
					.endVertex();
			}
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return renderType;
	}

	public static class Factory implements ParticleProvider<CubeParticleData> {

		public Factory() {}

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
