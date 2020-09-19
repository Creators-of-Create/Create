package com.simibubi.create.content.contraptions.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CubeParticle extends Particle {

	public static final Vec3d[] CUBE = {
		// TOP
		new Vec3d(1, 1, -1), new Vec3d(1, 1, 1), new Vec3d(-1, 1, 1), new Vec3d(-1, 1, -1),

		// BOTTOM
		new Vec3d(-1, -1, -1), new Vec3d(-1, -1, 1), new Vec3d(1, -1, 1), new Vec3d(1, -1, -1),

		// FRONT
		new Vec3d(-1, -1, 1), new Vec3d(-1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, -1, 1),

		// BACK
		new Vec3d(1, -1, -1), new Vec3d(1, 1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, -1, -1),

		// LEFT
		new Vec3d(-1, -1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, 1, 1), new Vec3d(-1, -1, 1),

		// RIGHT
		new Vec3d(1, -1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, -1), new Vec3d(1, -1, -1) };

	public static final Vec3d[] CUBE_NORMALS = {
		// modified normals for the sides
		new Vec3d(0, 1, 0), new Vec3d(0, -1, 0), new Vec3d(0, 0, 1), new Vec3d(0, 0, 1), new Vec3d(0, 0, 1),
		new Vec3d(0, 0, 1),

		/*
		 * new Vec3d(0, 1, 0), new Vec3d(0, -1, 0), new Vec3d(0, 0, 1), new Vec3d(0, 0,
		 * -1), new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0)
		 */
	};

	private static final IParticleRenderType renderType = new IParticleRenderType() {
		@Override
		public void beginRender(BufferBuilder builder, TextureManager textureManager) {
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

			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		}

		@Override
		public void finishRender(Tessellator tessellator) {
			tessellator.draw();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.disableLighting();
			RenderSystem.enableTexture();
		}
	};

	protected float scale;
	protected boolean hot;

	public CubeParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;

		setScale(0.2F);
	}

	public void setScale(float scale) {
		this.scale = scale;
		this.setSize(scale * 0.5f, scale * 0.5f);
	}

	public void averageAge(int age) {
		this.maxAge = (int) (age + (rand.nextDouble() * 2D - 1D) * 8);
	}
	
	public void setHot(boolean hot) {
		this.hot = hot;
	}
	
	private boolean billowing = false;
	
	@Override
	public void tick() {
		if (this.hot && this.age > 0) {
			if (this.prevPosY == this.posY) {
				billowing = true;
				field_228343_B_ = false; // Prevent motion being ignored due to vertical collision
				if (this.motionX == 0 && this.motionZ == 0) {
					Vec3d diff = new Vec3d(new BlockPos(posX, posY, posZ)).add(0.5, 0.5, 0.5).subtract(posX, posY, posZ);
					this.motionX = -diff.x * 0.1;
					this.motionZ = -diff.z * 0.1;
				}
				this.motionX *= 1.1;
				this.motionY *= 0.9;
				this.motionZ *= 1.1;
			} else if (billowing) {
				this.motionY *= 1.2;
			}
		}
		super.tick();
	}

	@Override
	public void buildGeometry(IVertexBuilder builder, ActiveRenderInfo renderInfo, float p_225606_3_) {
		Vec3d projectedView = renderInfo.getProjectedView();
		float lerpedX = (float) (MathHelper.lerp(p_225606_3_, this.prevPosX, this.posX) - projectedView.getX());
		float lerpedY = (float) (MathHelper.lerp(p_225606_3_, this.prevPosY, this.posY) - projectedView.getY());
		float lerpedZ = (float) (MathHelper.lerp(p_225606_3_, this.prevPosZ, this.posZ) - projectedView.getZ());

		// int light = getBrightnessForRender(p_225606_3_);
		int light = 15728880;// 15<<20 && 15<<4
		double ageMultiplier = 1 - Math.pow(age, 3) / Math.pow(maxAge, 3);

		for (int i = 0; i < 6; i++) {
			// 6 faces to a cube
			for (int j = 0; j < 4; j++) {
				Vec3d vec = CUBE[i * 4 + j];
				vec = vec
					/* .rotate(?) */
					.scale(scale * ageMultiplier)
					.add(lerpedX, lerpedY, lerpedZ);

				Vec3d normal = CUBE_NORMALS[i];
				builder.vertex(vec.x, vec.y, vec.z)
					.color(particleRed, particleGreen, particleBlue, particleAlpha)
					.texture(0, 0)
					.light(light)
					.normal((float) normal.x, (float) normal.y, (float) normal.z)
					.endVertex();
			}
		}
	}

	@Override
	public IParticleRenderType getRenderType() {
		return renderType;
	}

	public static class Factory implements IParticleFactory<CubeParticleData> {

		public Factory() {}

		@Override
		public Particle makeParticle(CubeParticleData data, World world, double x, double y, double z, double motionX,
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
